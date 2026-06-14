package it.uniroma3.siw.siw_football.service;

import it.uniroma3.siw.siw_football.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Procesa el login OAuth2 (Google). Tras obtener el perfil de Google,
 * busca el usuario por su email; si no existe, crea uno nuevo con rol USER
 * y provider GOOGLE (sin contraseña local). Devuelve el principal con la
 * autoridad ROLE_USER para que el resto de la app trate igual a estos usuarios.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google no devolvió un email para esta cuenta.");
        }

        // Upsert: el username es el email de Google.
        User user = userService.findByUsername(email);
        if (user == null) {
            user = new User(email, null, "USER", "GOOGLE");
            userService.saveUser(user);
        }

        String authority = "ROLE_" + user.getRole();
        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority(authority)),
            attributes,
            "email" // nameAttributeKey: lo que devuelve principal.getName()
        );
    }
}
