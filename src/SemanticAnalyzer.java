public class SemanticAnalyzer {
    private String[] declaredVariables = new String[100];
    private int varCount = 0;

    public String analyze(String code) {
        StringBuilder result = new StringBuilder("Resultat de l'analyse semantique :\n");
        String[] lines = code.split("\\n");

        boolean programStarted = false;
        boolean programEnded = false;
        boolean hasErrors = false;

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.equals("Snk_Begin")) {
                if (programStarted) {
                    result.append("Erreur: Le programme a deja commence\n");
                    hasErrors = true;
                } else {
                    programStarted = true;
                    result.append("Debut du programme detecte\n");
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
                    result.append("Fin du programme detectee\n");
                }
            } else if (trimmedLine.startsWith("Snk_Int") || trimmedLine.startsWith("Snk_Real")) {
                String[] parts = trimmedLine.split("\\s+");
                if (parts.length < 2) {
                    result.append("Erreur: La declaration doit contenir un type suivi d'un identificateur.\n");
                    hasErrors = true;
                } else {
                    String type = parts[0];
                    String[] identifiers = parts[1].split(",");
                    for (String identifier : identifiers) {
                        if (isDeclared(identifier)) {
                            result.append("Erreur: La variable '").append(identifier).append("' est deja declaree.\n");
                            hasErrors = true;
                        } else {
                            declaredVariables[varCount++] = identifier;
                            result.append("Declaration de ").append(type).append(" detectee : ").append(identifier).append("\n");
                        }
                    }
                }
            } else if (trimmedLine.startsWith("Set")) {
                String[] parts = trimmedLine.split("\\s+");
                if (parts.length != 3) {
                    result.append("Erreur de syntaxe dans l'affectation : ").append(trimmedLine).append("\n");
                    hasErrors = true;
                } else {
                    String variable = parts[1];
                    String value = parts[2];

                    if (!isDeclared(variable)) {
                        result.append("Erreur: Variable '").append(variable).append("' utilisee avant declaration.\n");
                        hasErrors = true;
                    } else {
                        if (value.matches("[0-9]+") || value.matches("[0-9]+\\.[0-9]+")) {
                            result.append("Affectation valide : ").append(trimmedLine).append("\n");
                        } else {
                            result.append("Erreur: Valeur non valide pour la variable '").append(variable).append("'.\n");
                            hasErrors = true;
                        }
                    }
                }
            } else if (trimmedLine.equals("# fin d’instruction")) {
                result.append("Fin d'instruction detectee\n");
            } else {
                result.append("Ligne inconnue ou semantiquement incorrecte : ").append(trimmedLine).append("\n");
                hasErrors = true;
            }
        }

        if (!programEnded) {
            result.append("Erreur: Le programme doit se terminer par Snk_End.\n");
            hasErrors = true;
        }

        if (hasErrors) {
            result.append("Le programme contient des erreurs semantiques.\n");
        } else {
            result.append("Le programme est semantiquement correct.\n");
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

    public static void main(String[] args) {
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        
        String testCode = "Snk_Begin\n" +
                          "Snk_Int x,y\n" +
                          "# fin d’instruction\n" +
                          "Set x 10\n" +
                          "Set y 20\n" +
                          "Snk_End";
        
        String result = analyzer.analyze(testCode);
        System.out.println(result);
    }
}

