package cf.cpc.uow.controller;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController {
	/**
	 * The home view.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(HttpServletRequest req,Locale locale, Model model) {
		System.out.println("Home Page Requested, locale = " + locale);
		return "home";
	}
}
