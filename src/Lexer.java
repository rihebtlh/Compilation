import java.util.regex.*;
import java.util.HashSet;

public class Lexer {

	private static final HashSet<String> KEYWORDS = new HashSet<>();
	private static final HashSet<String> SYMBOLS = new HashSet<>();
	private static final HashSet<String> OPERATORS = new HashSet<>();

	static {

		KEYWORDS.add("Snk_Begin");
		KEYWORDS.add("Snk_End");
		KEYWORDS.add("Begin");
		KEYWORDS.add("Snk_Int");
		KEYWORDS.add("Snk_Real");
		KEYWORDS.add("Set");
		KEYWORDS.add("If");
		KEYWORDS.add("Else");
		KEYWORDS.add("End");
		KEYWORDS.add("Snk_Print");

		SYMBOLS.add(",");
		SYMBOLS.add("#");
		SYMBOLS.add("[");
		SYMBOLS.add("]");

		OPERATORS.add("<");
		OPERATORS.add(">");
		OPERATORS.add("=");
		OPERATORS.add("!=");
	}

	private String getTokenExplanation(String token) {

		switch (token) {
		case "Snk_Begin":
			return "mot cle de debut de programme";
		case "Snk_End":
			return "mot cle de fin de programme";
		case "Snk_Real":
			return "mot cle de declaration du type reel";
		case "Snk_Int":
			return "mot cle de declaration du type entier";
		case "Set":
			return "mot cle pour affectation d’une valeur";
		case "If":
			return "mot cle pour conditionnel";
		case "Snk_Print":
			return "mot cle d'affichage";
		case "Begin":
			return "debut de bloc";
		case "Else":
			return "mot cle pour sinon";
		case "End":
			return "mot cle";
		case ",":
			return "separateur";
		case "#":
			return "fin d’instruction";
		case "##":
			return "Commantaire";
		case "[":
		case "]":
			return "debut/fin de condition";
		case "<":
		case ">":
		case "=":
		case "!=":
			return "operateur de comparaison";
		default:
			if (token.matches("\\d+")) {
				return "nombre entier";
			} else if (token.matches("\\d+\\.\\d+")) {
				return "nombre reel";
			} else if (token.matches("\"[^\"]*\"")) {
				return "chaîne de caracteres";
			} else if (token.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
				return "identificateur";
			}
			return "inconnu";
		}
	}

	public String tokenize(String input) {
		StringBuilder result = new StringBuilder();
		
		String expreg = "\"[^\"]*\"|\\d+\\.\\d+|\\d+|[a-zA-Z][a-zA-Z0-9_]*|[<>!=#\\[\\],]";//expression reguliere

		Pattern pattern = Pattern.compile(expreg);
		Matcher matcher = pattern.matcher(input);

		while (matcher.find()) {
			String token = matcher.group();

			if (KEYWORDS.contains(token)) {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			}

			else if (SYMBOLS.contains(token)) {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			} else if (OPERATORS.contains(token)) {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			} else {
				result.append(token).append(": ").append(getTokenExplanation(token)).append("\n");
			}
		}
		return result.toString();
	}
}
//Pattern.compile(String regex)
//Compile une expression régulière sous forme de Pattern.
//matcher(String input)
//matcher.find() : À chaque fois qu'un token est trouvé :
//Il est comparé avec les ensembles (KEYWORDS, SYMBOLS, OPERATORS).
//L'explication est ajoutée à result via getTokenExplanation().
