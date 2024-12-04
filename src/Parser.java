public class Parser {
	public String parse(String code) {
		StringBuilder result = new StringBuilder("Résultat de l'analyse syntaxique :\n");
		String[] lines = code.split("\n");

		boolean programStarted = false;
		boolean programEnded = false;
		boolean hasErrors = false;

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.equals("# fin d’instruction")) {
				result.append("Fin d’instruction détectée.\n");
				continue;
			}

			if (trimmedLine.equals("Snk_Begin")) {
				if (programStarted) {
					result.append("Erreur: Le programme a déjà commencé.\n");
					hasErrors = true;
				} else {
					programStarted = true;
					result.append("Début du programme détecté.\n");
				}
			} else if (trimmedLine.equals("Snk_End")) {
				if (!programStarted) {
					result.append("Erreur: Le programme doit commencer par Snk_Begin.\n");
					hasErrors = true;
				}
				if (programEnded) {
					result.append("Erreur: Le programme a déjà été terminé.\n");
					hasErrors = true;
				} else {
					programEnded = true;
					result.append("Fin du programme détectée.\n");
				}
			} else if (trimmedLine.endsWith("#")) {
				result.append("Fin d’instruction détectée : ").append(trimmedLine).append("\n");
			}

			else if (trimmedLine.startsWith("Snk_Int") || trimmedLine.startsWith("Snk_Real")) {
				String[] parts = trimmedLine.split("\\s+");
				if (parts.length < 3) {
					result.append("Erreur: La déclaration doit contenir un type suivi d'un identificateur.\n");
					hasErrors = true;
				} else {

					boolean validDeclaration = true;
					String[] identifiers = parts[1].split(",");
					for (String identifier : identifiers) {
						String trimmedIdentifier = identifier.trim();
						if (!trimmedIdentifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
							validDeclaration = false;
							result.append(
									"Erreur: La déclaration doit contenir des identificateurs valides. Identifiant : ")
									.append(trimmedIdentifier).append("\n");
							hasErrors = true;
							break;
						}
					}
					if (validDeclaration) {
						result.append("Déclaration de type détectée : ").append(trimmedLine).append("\n");
					}
				}
			}
			// Vérification des affectations
			else if (trimmedLine.startsWith("Set")) {
				String[] parts = trimmedLine.split("\\s+");
				if (parts.length != 3 || !parts[1].matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
					result.append("Erreur de syntaxe dans l'affectation : ").append(trimmedLine).append("\n");
					hasErrors = true;
				} else {
					result.append("Affectation détectée : ").append(trimmedLine).append("\n");
				}
			}
			// Vérification des structures conditionnelles (If, Else, Begin)
			else if (trimmedLine.startsWith("If") || trimmedLine.startsWith("Else")
					|| trimmedLine.startsWith("Begin")) {
				result.append("Mot-clé détecté : ").append(trimmedLine).append("\n");
			}
			// Vérification des expressions avec des crochets [ ] (ex: If [ i < 20 ])
			else if (trimmedLine.matches("If \\[.*\\]") || trimmedLine.matches("Else")
					|| trimmedLine.matches("Begin")) {
				result.append("Expression conditionnelle ou début de bloc détecté : ").append(trimmedLine).append("\n");
			}
			// Vérification des appels de fonction comme Snk_Print
			else if (trimmedLine.startsWith("Snk_Print")) {
				result.append("Fonction d'affichage détectée : ").append(trimmedLine).append("\n");
			}
			// Vérification de la ligne "End"
			else if (trimmedLine.equals("End")) {
				result.append("Mot-clé détecté : End\n");
			} else {
				result.append("Ligne inconnue ou syntaxe incorrecte : ").append(trimmedLine).append("\n");
				hasErrors = true;
			}
		}

		if (!programEnded) {
			result.append("Erreur: Le programme doit se terminer par Snk_End.\n");
			hasErrors = true;
		}

		if (hasErrors) {
			result.append("Le programme contient des erreurs de syntaxe.\n");
		} else {
			result.append("Le programme est syntaxiquement correct.\n");
		}

		return result.toString();
	}

	public static void main(String[] args) {
		Parser parser = new Parser();

		String testCode = "Snk_Begin\n" + "Snk_Int i, j\n" + "# fin d’instruction\n" + "Snk_Real x1\n"
				+ "# fin d’instruction\n" + "Set i 10\n" + "# fin d’instruction\n" + "If [ i < 20 ]\n" + "Set j 5\n"
				+ "# fin d’instruction\n" + "Else\n" + "Begin\n" + "Set x1 15.5\n" + "# fin d’instruction\n" + "End\n"
				+ "Snk_Print \"Hello, World!\"\n" + "# fin d’instruction\n" + "Snk_End";

		// Analyser le code et afficher les résultats
		String result = parser.parse(testCode);
		System.out.println(result);
	}
}
