package app.mainichi.session

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.server.session.DefaultWebSessionManager
import org.springframework.web.server.session.WebSessionManager
import org.springframework.web.server.session.WebSessionStore

@Component("webSessionManager")
class R2dbcWebSessionManager(
    delegate: WebSessionManager
) : WebSessionManager by delegate {
    @Autowired
    constructor(sessionStore: WebSessionStore) :
            this(DefaultWebSessionManager().apply {
                setSessionStore(sessionStore)
            })
}