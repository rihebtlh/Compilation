public class SemanticAnalyzer {
    private String[] declaredVariables = new String[100];
    private int varCount = 0;

    public String analyze(String code) {
        StringBuilder result = new StringBuilder("Résultat de l'analyse sémantique :\n");
        String[] lines = code.split("\\n");

        boolean programStarted = false;
        boolean programEnded = false;
        boolean hasErrors = false;

        // Process code line by line
        for (String line : lines) {
            String trimmedLine = line.trim();

            // Skip empty lines
            if (trimmedLine.isEmpty()) {
                continue;
            }

            // Handle comments (lines starting with ##)
            if (trimmedLine.startsWith("##")) {
                result.append("Commentaire : ").append(trimmedLine).append("\n");
                continue;  // Skip processing this line
            }

            // Handle program start (Snk_Begin)
            if (trimmedLine.equals("Snk_Begin")) {
                if (programStarted) {
                    result.append("Erreur: Le programme a déjà commencé\n");
                    hasErrors = true;
                } else {
                    programStarted = true;
                    result.append("Début du programme détecté\n");
                }
                continue;  // No need to check for # here
            }

            // Handle program end (Snk_End)
            if (trimmedLine.equals("Snk_End")) {
                if (!programStarted) {
                    result.append("Erreur: Le programme doit commencer par Snk_Begin\n");
                    hasErrors = true;
                }
                if (programEnded) {
                    result.append("Erreur: Le programme a déjà été terminé\n");
                    hasErrors = true;
                } else {
                    programEnded = true;
                    result.append("Fin du programme détectée\n");
                }
                continue;  // No need to check for # here
            }

            // Handle conditional blocks (If, Else, Begin, End)
            if (trimmedLine.startsWith("If") || trimmedLine.startsWith("Else") || trimmedLine.equals("Begin") || trimmedLine.equals("End")) {
                result.append("Conditionnelle ou début/fin de bloc : ").append(trimmedLine).append("\n");
                continue;  // No need to check for # here
            }

            // Check if lines that should end with "#" (like Set, Snk_Int, Snk_Real) do so
            if (trimmedLine.endsWith("#")) {
                // Remove the '#' for further processing
                trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 1).trim();
            } else if (trimmedLine.startsWith("Set") || trimmedLine.startsWith("Snk_Int") || trimmedLine.startsWith("Snk_Real")) {
                result.append("Erreur: L'instruction doit se terminer par ' #'. Ligne : ").append(trimmedLine).append("\n");
                hasErrors = true;
                continue;  // Skip processing this line
            }

            // Handle declarations (Snk_Int, Snk_Real)
            if (trimmedLine.startsWith("Snk_Int") || trimmedLine.startsWith("Snk_Real")) {
                String[] parts = trimmedLine.split("\\s+");
                if (parts.length < 2) {
                    result.append("Erreur: La déclaration doit contenir un type suivi d'un identificateur.\n");
                    hasErrors = true;
                } else {
                    String type = parts[0];
                    String[] identifiers = parts[1].split(",");
                    for (String identifier : identifiers) {
                        identifier = identifier.trim();
                        if (isDeclared(identifier)) {
                            result.append("Erreur: La variable '").append(identifier).append("' est déjà déclarée.\n");
                            hasErrors = true;
                        } else {
                            declaredVariables[varCount++] = identifier;
                            result.append("Déclaration de ").append(type).append(" détectée : ").append(identifier).append("\n");
                        }
                    }
                }
            }
            // Handle assignment statements (Set)
            else if (trimmedLine.startsWith("Set")) {
                String[] parts = trimmedLine.split("\\s+");
                if (parts.length != 3) {
                    result.append("Erreur de syntaxe dans l'affectation : ").append(trimmedLine).append("\n");
                    hasErrors = true;
                } else {
                    String variable = parts[1];
                    String value = parts[2];

                    // Check if variable is declared before use
                    if (!isDeclared(variable)) {
                        result.append("Erreur: Variable '").append(variable).append("' utilisée avant déclaration.\n");
                        hasErrors = true;
                    } else {
                        // Check if the assignment is valid for the declared type
                        if (value.matches("[0-9]+")) {  // Integer assignment
                            result.append("Affectation valide (entier) : ").append(trimmedLine).append("\n");
                        } else if (value.matches("[0-9]+\\.[0-9]+")) {  // Real assignment
                            result.append("Affectation valide (réel) : ").append(trimmedLine).append("\n");
                        } else {
                            result.append("Erreur: Valeur non valide pour la variable '").append(variable).append("'.\n");
                            hasErrors = true;
                        }
                    }
                }
            } 
            // Handle Print statements (Snk_Print)
            else if (trimmedLine.startsWith("Snk_Print")) {
                result.append("Affichage : ").append(trimmedLine).append("\n");
            }
            else {
                result.append("Ligne inconnue ou sémantiquement incorrecte : ").append(trimmedLine).append("\n");
                hasErrors = true;
            }
        }

        if (!programEnded) {
            result.append("Erreur: Le programme doit se terminer par Snk_End.\n");
            hasErrors = true;
        }

        if (hasErrors) {
            result.append("Le programme contient des erreurs sémantiques.\n");
        } else {
            result.append("Le programme est sémantiquement correct.\n");
        }

        return result.toString();
    }

    private boolean isDeclared(String variable) {
        for (int i = 0; i < varCount; i++) {
            if (declaredVariables[i].equals(variable)) {
                return true;
            }
        }
        return false;
    }
}