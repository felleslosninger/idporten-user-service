package no.idporten.userservice.logging;

public class TokenMasker {

    private TokenMasker() {
    }

    public static String maskToken(final String token) {
        if (token == null) {
            return token;
        }
        if (token.contains(".")) { // jwt
            return token.substring(0, token.lastIndexOf('.')) + "...";
        }
        if (token.length() > 10) { //opaque or test-token
            return token.substring(0, 10) + "...";
        }
        return token;
    }

}
