package com.rtu.gmall.sso.controller;

import com.rtu.gmall.sso.config.SsoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HelloController {

    @Autowired
    SsoConfig ssoConfig;
    @GetMapping("/")
    public String index(Model model,
                        @CookieValue(value="sso_user", required = false) String cookieVal,
                        @RequestParam(value="sso_user", required = false) String token,
                        HttpServletRequest request, HttpServletResponse response) {
        if(!StringUtils.isEmpty(token)) {
            Cookie cookie = new Cookie("sso_user", token);
            response.addCookie(cookie);
            return "index";
        }

        if(StringUtils.isEmpty(cookieVal)) {
            return "redirect:" + ssoConfig.getUrl() + ssoConfig.getLoginPath() + "?redirectUrl=" + request.getRequestURL();
        } else {
            model.addAttribute("loginUser", "张三");
            return "index";
        }
    }
}
