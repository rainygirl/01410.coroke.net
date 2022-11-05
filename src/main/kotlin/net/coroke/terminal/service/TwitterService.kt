package net.coroke.terminal.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.social.connect.Connection
import org.springframework.social.oauth1.AuthorizedRequestToken
import org.springframework.social.oauth1.OAuth1Operations
import org.springframework.social.oauth1.OAuth1Parameters
import org.springframework.social.oauth1.OAuthToken
import org.springframework.social.twitter.api.Twitter
import org.springframework.social.twitter.connect.TwitterConnectionFactory
import org.springframework.stereotype.Controller
import javax.servlet.http.HttpServletRequest

@Controller
class TwitterService {
    @Value("\${twitter.client-id}")
    private var clientId: String = ""

    @Value("\${twitter.client-secret}")
    private var clientSecret: String = ""

    @Value("\${twitter.callback-uri}")
    private var callbackUri: String = ""

    fun getAuthenticationUrl(request: HttpServletRequest): String {
        val operations: OAuth1Operations = TwitterConnectionFactory(clientId, clientSecret).getOAuthOperations()
        val scheme = request.getHeader("X-Forwarded-Proto")
        val urlPrefix = (scheme ?: request.scheme) + "://" + request.getHeader(HttpHeaders.HOST)
        val callbackURL = urlPrefix + callbackUri
        val oAuthToken: OAuthToken = operations.fetchRequestToken(callbackURL, null)

        request.servletContext.setAttribute("token", oAuthToken)

        return operations.buildAuthenticateUrl(oAuthToken.getValue(), OAuth1Parameters())
    }

    fun getAccessTokenToConnection(
        request: HttpServletRequest,
        oauthVerifier: String
    ): Connection<Twitter> {

        val twitterConnectionFactory = TwitterConnectionFactory(clientId, clientSecret)
        val operations: OAuth1Operations = twitterConnectionFactory.getOAuthOperations()
        val requestToken: OAuthToken = request.servletContext.getAttribute("token") as OAuthToken
        val accessToken: OAuthToken =
            operations.exchangeForAccessToken(AuthorizedRequestToken(requestToken, oauthVerifier), null)

        request.servletContext.removeAttribute("token")
        return twitterConnectionFactory.createConnection(accessToken)
    }

    fun getUserProfileMap(connection: Connection<Twitter>): MutableMap<String, String> {

        val map: MutableMap<String, String> = HashMap()
        val profile = connection.fetchUserProfile()
        map["origin"] = "twitter"
        map["name"] = profile.name ?: ""
        map["email"] = profile.email ?: ""
        map["picture"] = connection.imageUrl ?: ""
        map["username"] = connection.displayName.substring(1)
        map["uid"] = connection.key.providerUserId

        return map
    }
}
