public class Parser {
	public String parse(String code) {
		StringBuilder result = new StringBuilder("Resultat de l'analyse syntaxique :\n");
		String[] lines = code.split("\n");

		boolean programStarted = false;
		boolean programEnded = false;
		boolean hasErrors = false;

		for (String line : lines) {
			String trimmedLine = line.trim();

			if (trimmedLine.equals("# fin d'instruction")) {
				result.append("Fin d'instruction detectee\n");
				continue;
			}

			if (trimmedLine.equals("Snk_Begin")) {
				if (programStarted) {
					result.append("Erreur: Le programme a deja commence\n");
					hasErrors = true;
				} else {
					programStarted = true;
					result.append("Debut du programme\n");
				}
			} else if (trimmedLine.equals("Snk_End")) {
				if (!programStarted) {
					result.append("Erreur: Le programme doit commencer par Snk_Begin\n");
					hasErrors = true;
				}
				if (programEnded) {
					result.append("Erreur: Le programme a deja ete termine\n");
					hasErrors = true;
				} else {
					programEnded = true;
					result.append("Fin du programme\n");
				}
			} 
			else if (trimmedLine.startsWith("##")) {
	            result.append("Commentaire detecte : ").append(trimmedLine).append("\n");
	            continue;
	        }
			else if (trimmedLine.startsWith("Snk_Int")) {
				result.append("Declaration de variables entieres : ").append(trimmedLine).append("\n");
			}
			else if (trimmedLine.startsWith("Snk_Real")) {
				result.append("Declaration de variables reelles : ").append(trimmedLine).append("\n");
			}
			else if (trimmedLine.startsWith("Set")) {
				result.append("Affectation d’une valeur : ").append(trimmedLine).append("\n");
			}
			else if (trimmedLine.startsWith("Get")) {
				result.append("Affectation de valeur entre 2 variables : ").append(trimmedLine).append("\n");
			}
			else if (trimmedLine.matches("Snk_Print \".*\" #")) {
				result.append("Affichage d'un message : ").append(trimmedLine).append("\n");
			}
			else if (trimmedLine.matches("Snk_Print [a-zA-Z_][a-zA-Z0-9_, ]* #")) {
				result.append("Affichage de la valeur de variables : ").append(trimmedLine).append("\n");
			}
			else if (trimmedLine.matches("If \\[.*\\]")) {
				result.append("Condition If : ").append(trimmedLine).append("\n");
			}
			else if (trimmedLine.equals("Else")) {
				result.append("Instruction conditionnelle Else\n");
			}
			else if (trimmedLine.equals("Begin")) {
				result.append("Debut de bloc : Begin\n");
			}
			else if (trimmedLine.equals("End")) {
				result.append("Fin de bloc: End\n");
			}
			 else if (isArithmeticExpression(trimmedLine)) { // Ignore les expressions arithmétiques
	                continue; // Ne fait rien pour ces lignes
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
	
	 private boolean isArithmeticExpression(String line) {
	        // Vérifie si c'est une expression arithmétique ou une affectation avec un #
	        return line.matches(".*[\\d\\+\\-\\*/\\^\\(\\)=].*#");
	    }
}
