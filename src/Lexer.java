import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {

	// Source unique de vérité : token connu -> explication
	private static final Map<String, String> TOKENS = new HashMap<>();

	static {
		// Mots-clés
		TOKENS.put("Snk_Begin", "mot cle de debut de programme");
		TOKENS.put("Snk_End", "mot cle de fin de programme");
		TOKENS.put("Snk_Int", "mot cle de declaration du type entier");
		TOKENS.put("Snk_Real", "mot cle de declaration du type reel");
		TOKENS.put("Snk_Strg", "mot cle de declaration du type chaine");
		TOKENS.put("Snk_Print", "mot cle d'affichage");
		TOKENS.put("Set", "mot cle pour affectation d'une valeur");
		TOKENS.put("Get", "mot cle pour recuperer une valeur");
		TOKENS.put("from", "mot cle d'origine (utilise avec Get)");
		TOKENS.put("If", "mot cle pour conditionnel");
		TOKENS.put("Else", "mot cle pour sinon");
		TOKENS.put("Begin", "debut de bloc");
		TOKENS.put("End", "fin de bloc");

		// Symboles
		TOKENS.put(",", "separateur");
		TOKENS.put("#", "fin d'instruction");
		TOKENS.put("##", "commentaire");
		TOKENS.put("[", "debut de condition");
		TOKENS.put("]", "fin de condition");
		TOKENS.put("(", "parenthese ouvrante");
		TOKENS.put(")", "parenthese fermante");

		// Opérateurs de comparaison
		TOKENS.put("<", "operateur de comparaison");
		TOKENS.put(">", "operateur de comparaison");
		TOKENS.put("=", "operateur de comparaison");
		TOKENS.put("!=", "operateur de comparaison");

		// Opérateurs arithmétiques
		TOKENS.put("+", "operateur arithmetique");
		TOKENS.put("-", "operateur arithmetique");
		TOKENS.put("*", "operateur arithmetique");
		TOKENS.put("/", "operateur arithmetique");
		TOKENS.put("^", "operateur arithmetique (puissance)");
	}

	// L'ordre des alternatives est important : les plus specifiques d'abord
	private static final Pattern PATTERN = Pattern.compile(
			"##[^\\r\\n]*"                          // commentaire (jusqu'a la fin de ligne)
			+ "|\"[^\"\\r\\n]*\""                   // chaine de caracteres
			+ "|\"[^\"\\r\\n]*"                     // chaine non terminee
			+ "|\\d+[a-zA-Z_][a-zA-Z0-9_]*"         // identifiant invalide (ex: 2x)
			+ "|\\d+\\.\\d+"                        // nombre reel
			+ "|\\d+"                               // nombre entier
			+ "|[a-zA-Z_][a-zA-Z0-9_]*"             // identificateur / mot cle
			+ "|!=|[<>=#\\[\\],+\\-*/^()]"          // operateurs et symboles
			+ "|\\S");                              // tout autre caractere => erreur lexicale

	private String getTokenExplanation(String token) {
		String known = TOKENS.get(token);
		if (known != null) {
			return known;
		}
		if (token.startsWith("\"")) {
			return (token.length() >= 2 && token.endsWith("\""))
					? "chaine de caracteres"
					: "chaine non terminee (erreur lexicale)";
		}
		if (token.matches("\\d+")) {
			return "nombre entier";
		}
		if (token.matches("\\d+\\.\\d+")) {
			return "nombre reel";
		}
		if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
			return "identificateur";
		}
		if (token.matches("\\d+[a-zA-Z_][a-zA-Z0-9_]*")) {
			return "identificateur invalide (commence par un chiffre)";
		}
		return "inconnu (caractere non reconnu)";
	}

	public String tokenize(String input) {
		StringBuilder result = new StringBuilder();
		Matcher matcher = PATTERN.matcher(input);

		while (matcher.find()) {
			String token = matcher.group();

			// Le contenu d'un commentaire n'est pas decoupe en tokens
			if (token.startsWith("##")) {
				token = "##";
			}

			result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
		}
		return result.toString();
	}
}
