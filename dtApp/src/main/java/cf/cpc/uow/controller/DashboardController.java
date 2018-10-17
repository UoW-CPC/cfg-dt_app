package cf.cpc.uow.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import org.json.JSONObject;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

@Controller
@PropertySource(value= {"classpath:config.properties"})
public class DashboardController {
	
	@Value("${cfgum.api.base.url}")
	private String CFGUM_API_BASE_URL;
	
	@Value("${cfgum.client.id}")
	private String CFGUM_CLIENT_ID;
	
	@Value("${cfgum.client.secret}")
	private String CFGUM_CLIENT_SECRET;
	
	// ------------------------------------------------
	String tokenString="";
	String idTokenString = "";
	String refreshTokenString = "";
	String displayMessage = "";
    List<String> userInfo = Collections.emptyList();
    String refreshResult = "";
    String dmTokenString = "";
    IDToken idToken = null;
    List<String> claimsList = Collections.emptyList();
    List<String> apiClaimsList = Collections.emptyList();
    List<String> dmClaimsList = Collections.emptyList();
    List<String> dmAPIClaimsList = Collections.emptyList();
    
	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
	public String home(HttpServletRequest req,Locale locale, Model model) {
		System.out.println("DT Dashboard Page Requested, locale = " + locale);
		
		// Check if url contains query parameters,
		String queryString = req.getQueryString();
		System.out.println("Query String:" + queryString);
		if(queryString != null && queryString.isEmpty()==false) {
			String[] keyValue=queryString.split("=");
			if(keyValue.length==2 && keyValue[0].equals("token")) {
				dmTokenString = keyValue[1];
				dmClaimsList = verify(dmTokenString);
				dmAPIClaimsList = verifyUsingAPI(dmTokenString);
			}
			else if(keyValue.length==2 && keyValue[0].equals("displayMessage")){
				System.out.println("Else condition");
				model.addAttribute("displayMessage",displayMessage);
			}
		}else {
			dmTokenString = "";
			dmClaimsList = Collections.emptyList();
			dmAPIClaimsList = Collections.emptyList();
		}
		
		// Obtain {access, id, and refresh} tokens, get user info and verify token.
		getVariousToken(req);
		getUserInfo();
		verifyToken();
		
		// Set model attributes to be retreived in jsp page.
		model.addAttribute("accessToken", tokenString);
		model.addAttribute("idTokenString", idTokenString);
		model.addAttribute("refreshTokenString", refreshTokenString);
		model.addAttribute("idToken",idToken);
		model.addAttribute("userInfo", userInfo);
		model.addAttribute("claimsList",claimsList);
		model.addAttribute("apiClaimsList",apiClaimsList);
		model.addAttribute("dmTokenString", dmTokenString);
		model.addAttribute("dmClaimsList",dmClaimsList);
		model.addAttribute("dmAPIClaimsList",dmAPIClaimsList);
		return "dashboard";
	}
	/**
	 * This function retrieves {access Token, id token and refresh token} from the HttpServletRequest.
	 * @param req the current HttpServletRequest instance.
	 */
	public void getVariousToken(HttpServletRequest req) {
		KeycloakSecurityContext kcContext = (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
		
	    RefreshableKeycloakSecurityContext ctx = (RefreshableKeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
	    
	    tokenString = kcContext.getTokenString();
		idTokenString = kcContext.getIdTokenString();
		refreshTokenString = ctx.getRefreshToken();
		idToken = kcContext.getIdToken();
	}
	public List<String> jsonToList(String _jsonString){
		List<String> resList = new ArrayList<String>();
		JSONObject obj = new JSONObject(_jsonString);
		Iterator<String> keysItr = obj.keys();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			String val = obj.get(key).toString();
			resList.add(key + ":" + val);
		}
		return resList;
	}
	 
	/**
	 * This function calls userinfo [GET] API 
	 */
	public void getUserInfo() {
		Client client = ClientBuilder.newClient();
		String uri = CFGUM_API_BASE_URL + "/userinfo/" + tokenString;
	    WebTarget target = client.target (uri);
	    
	    String result = target.request(MediaType.APPLICATION_JSON)
	           .get(String.class);
	   userInfo  = jsonToList(result);
	}

	public void verifyToken() {
		// Verify Locally
		claimsList = verify(tokenString);
		// Verify using API
		apiClaimsList = verifyUsingAPI(tokenString);
	}
	public List<String> verifyUsingAPI(String _token){
		// Verify using API
		Client client = ClientBuilder.newClient();
		String uri = CFGUM_API_BASE_URL + "/tokens/" + _token;
		WebTarget target = client.target(uri).queryParam("client_id", CFGUM_CLIENT_ID).queryParam("client_secret",
				CFGUM_CLIENT_SECRET);

		String result = target.request(MediaType.APPLICATION_JSON).get(String.class);
		List<String> claims = jsonToList(result);// Arrays.asList(result.substring(1, result.length()-1).split(","));
		return claims;
	}
	/**
	 * This function verifies a token and returns the list of claims in the case of valid token. 
	 * Alternatively, it returns an empty list.
	 * @param _token the input access token
	 * @return list of claims.
	 */
	private ArrayList<String> verify(String _token){
		ArrayList<String> claims = new ArrayList<String>();
		try {
			String kId = JWT.decode(_token).getKeyId();	// Decode token to obtain key identifier.
			JwkProvider provider = new UrlJwkProvider(new URL(getPublicKeyEndpoint()));
			Jwk jwk = provider.get(kId);
			RSAPublicKey rpk = (RSAPublicKey) jwk.getPublicKey();
			Algorithm algorithm = Algorithm.RSA256(rpk, null); // Private key is null in this case.
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT jwt = verifier.verify(_token);	// This operation generates a JWTVerificationException, if token is invalid.
			for (Map.Entry<String, Claim> entry : jwt.getClaims().entrySet()) {
				Claim claim = entry.getValue();
				if (claim.isNull() == false) {
					String claimValue = claim.asString();
					if (claimValue == null) {
						claimValue = getClaimValue(claim);
					}
					claims.add(entry.getKey() + ":" + claimValue);
				}
			}
		}catch (JWTVerificationException verExc) {
			// Invalid signature/claims
			claims.add("Error message (verify token):" + verExc.getMessage());
		}catch (MalformedURLException malExc) {
			claims.add("Error message (verify token):" + malExc.getMessage());
		}catch (JwkException jwkExc) {
			claims.add("Error message (verify token):" + jwkExc.getMessage());
		}
		return claims;
	}
	/**
	 * This function obtains the public key endpoint by calling the rest api.
	 * @return the obtained endpoint.
	 */
	public String getPublicKeyEndpoint() {
		Client client = ClientBuilder.newClient();
		String uri = CFGUM_API_BASE_URL + "/endpoint";
	    WebTarget target = client.target(uri);
	    
	    Response endpoint = target.request(MediaType.APPLICATION_JSON)
	           .get();
	    if (endpoint.getStatus() == 200) {
	    	JSONObject jo = new JSONObject(endpoint.readEntity(String.class));
	    	return jo.getString("pk_endpoint");
	    }
	    return "";
	}
	/**
	 * This function extracts the value from the claim object. 
	 * @param _claim The claim object.
	 * @return the string representation of claim value.
	 */
	public static String getClaimValue(Claim _claim) {
		String retValue="";
		if (_claim.asInt()!=null)
			retValue =  _claim.asInt().toString();
		else if(_claim.asBoolean()!=null)
			retValue = _claim.asBoolean().toString();
		else if(_claim.asDate()!=null)
			retValue = _claim.asDate().toString();
		else if(_claim.asMap()!=null)
			retValue = _claim.asMap().toString();
		else if(_claim.asList(String.class)!=null)
			retValue = _claim.asList(String.class).toString(); 
		else
			retValue = _claim.toString();
		return retValue;
	}
	/**
	 * This function logout the application by calling the respective API.
	 * @param req the HttpServletRequest
	 * @param model the model variable
	 * @return the redirected page after the completion of logout process.
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public String logout(HttpServletRequest req, Model model) {
		System.out.println("Logout (keycloak)");
		
		Client client = ClientBuilder.newClient();
		String uri = CFGUM_API_BASE_URL + "/tokens/" + refreshTokenString;
	    WebTarget target = client.target(uri);
	       
	    String payload = "{\"client_id\":\"" + CFGUM_CLIENT_ID + "\",\"client_secret\":\"" + CFGUM_CLIENT_SECRET + "\"}";
	    System.out.println("Payload:" + payload);
	    		
	    Response result = target.request(MediaType.APPLICATION_JSON)
	    		.build("DELETE", Entity.json(payload)).invoke();
	    if(result.getStatus() == 200) {
	    	System.out.println("Logout successful");
	    	return "redirect:/";
	    }
	    else {
	    	System.out.println("Logout result:" + result.toString());
	    	displayMessage = "Logout result:" + result.readEntity(String.class);
		    model.addAttribute("displayMessage",displayMessage);
		    return "redirect:/dashboard";
	    }
	}
}
