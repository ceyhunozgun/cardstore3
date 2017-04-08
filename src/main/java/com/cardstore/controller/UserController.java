package com.cardstore.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cardstore.dao.UserRepository;
import com.cardstore.entity.User;

@Controller
public class UserController {

	public static final String USER_KEY_FOR_SESSION = "USER";

	@Autowired
	UserRepository userRepository;

	@RequestMapping("/")
	public String home(Map<String, Object> model, HttpSession session) {

		User user = userfromSession(session);

		if (user == null)
			return "index";
		else {
			// get up to date balance information from table, because balance
			// can be changed any time.
			User uptoDate = userRepository.findOne(user.getUsername());

			user.setBalance(uptoDate.getBalance());

			model.put("user", user);
			return "dashboard";
		}
	}

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	@ResponseBody
	public boolean registerUser(@RequestBody User user) {

		User previous = userRepository.findOne(user.getUsername());

		if (previous == null) {
			user.setBalance(100);
			userRepository.save(user);
		}

		return previous == null;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST, produces = "text/plain")
	@ResponseBody
	public String login(@RequestBody User user, HttpServletRequest request) {
		String error = "None";
		User existing = userRepository.findOne(user.getUsername());

		boolean canLogin = existing != null && existing.getPassword().equals(user.getPassword());

		if (!canLogin)
			error = "User name and password mismatch.";
		else {
			HttpSession session = request.getSession(true);

			session.setAttribute(USER_KEY_FOR_SESSION, existing);
		}
		return error;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	@ResponseBody
	public boolean logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		if (session != null)
			session.invalidate();
		return true;
	}

	public static User userfromSession(HttpSession session) {
		User user = (User) session.getAttribute(USER_KEY_FOR_SESSION);

		return user;
	}

	public static boolean loggedIn(HttpSession session) {
		return userfromSession(session) != null;
	}
}
