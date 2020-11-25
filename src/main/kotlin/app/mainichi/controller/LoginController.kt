package app.mainichi.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController {
    @GetMapping("/api/authorize")
    fun authorize() {
        
    }
}