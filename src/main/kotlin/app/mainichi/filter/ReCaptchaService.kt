package app.mainichi.filter

import app.mainichi.ErrorCode
import app.mainichi.component.ResponseStatusCodeException
import app.mainichi.response.ReCaptchaTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class ReCaptchaService(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Value("\${google.recaptcha.secret}")
    private val secret: String,
) {
    private val rest = RestTemplate()

    fun validate(captcha: String): Boolean {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map = LinkedMultiValueMap<String, String>()
        map.add("secret", secret)
        map.add("response", captcha)

        val captchaVerify = rest.postForEntity(
            "https://www.google.com/recaptcha/api/siteverify",
            HttpEntity<MultiValueMap<String, String>>(
                map,
                headers
            ),
            ReCaptchaTokenResponse::class.java
        )

        val captchaBody = captchaVerify.body
        if (captchaVerify.statusCode != HttpStatus.OK || captchaBody == null || !captchaBody.success)
            return false

        return true
    }
}