package edu.nju.codefeature;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    @RequestMapping("/")
    public String index(@RequestParam String path, HttpSession session) {
        session.setAttribute("predictPath", path);
        return "index.html";
    }

}
