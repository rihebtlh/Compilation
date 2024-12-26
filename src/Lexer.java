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
			return "mot cle de déclaration du type réel";
		case "Snk_Int":
			return "mot cle de déclaration du type entier";
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
			return "début/fin de condition";
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

		String regex = "\"[^\"]*\"|\\d+\\.\\d+|\\d+|[a-zA-Z][a-zA-Z0-9_]*|[<>!=#\\[\\],]";

		Pattern pattern = Pattern.compile(regex);
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

	public static void main(String[] args) {
		Lexer lexer = new Lexer();

		String testCode = "Snk_Begin\n" + "Snk_Int i, j\n" + "# fin d’instruction\n" + "Snk_Real x1\n"
				+ "# fin d’instruction\n" + "Set i 10\n" + "# fin d’instruction\n" + "If [ i < 20 ]\n" + "Set j 5\n"
				+ "# fin d’instruction\n" + "Else\n" + "Begin\n" + "Set x1 15.5\n" + "# fin d’instruction\n" + "End\n"
				+ "Snk_Print \"Hello, World!\"\n" + "# fin d’instruction\n" + "Snk_End";

		String result = lexer.tokenize(testCode);
		System.out.println(result);
	}
}
