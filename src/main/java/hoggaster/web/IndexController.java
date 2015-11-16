package hoggaster.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by svante2 on 2015-11-16.
 */
@Controller
@RequestMapping("/")
public class IndexController {


    @RequestMapping
    public String home() {
        return "redirect:swagger-ui.html";
    }

}
