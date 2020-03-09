package com.rtu.gmall.sso.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/login")
    public String login(@RequestParam(value="redirectUrl", required = false) String redirectUrl,
                        @CookieValue(value="sso_user", required = false) String ssoUser,
                        Model model) {
        if(!StringUtils.isEmpty(ssoUser)) {
            return "redirect:" + redirectUrl + "?" + "sso_user=" + ssoUser;
        } else {
            model.addAttribute("redirectUrl", redirectUrl);
            return "login";
        }
    }

    @PostMapping("/doLogin")
    public void doLogin(String username, String password, String redirectUrl,
                          HttpServletResponse response) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("passowrd", password);
        String id = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(id, JSON.toJSONString(map));

        Cookie cookie = new Cookie("sso_user", id);
        response.addCookie(cookie);
        response.sendRedirect(redirectUrl + "?sso_user=" + id);
    }
}
