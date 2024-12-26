<<<<<<< HEAD
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
=======
package snake;

public class SemanticAnalyzer {
    private final java.util.Map<String, String> variableTypes = new java.util.HashMap<>(); // Store declared variable types
    private final java.util.regex.Pattern intPattern = java.util.regex.Pattern.compile("\\d+"); // Pattern for integers
    private final java.util.regex.Pattern realPattern = java.util.regex.Pattern.compile("\\d+\\.\\d+"); // Pattern for reals
    private final java.util.regex.Pattern stringPattern = java.util.regex.Pattern.compile("\"[^\"]*\""); // Pattern for strings
    private final java.util.regex.Pattern identifierPattern = java.util.regex.Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*"); // Valid identifier

    public String analyze(String code) {
        StringBuilder result = new StringBuilder("Semantic Analysis Result:\n");
        String[] lines = code.split("\\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Snk_Int") || line.startsWith("Snk_Real") || line.startsWith("Snk_Strg")) {
                handleDeclaration(line, result);
            } else if (line.startsWith("Set")) {
                handleAssignment(line, result);
            } else if (line.startsWith("Get")) {
                handleGet(line, result);
            } else if (line.startsWith("Snk_Print")) {
                handlePrint(line, result);
            }
        }

        return result.toString();
    }

    private void handleDeclaration(String line, StringBuilder result) {
        String type;
        if (line.startsWith("Snk_Int")) {
            type = "int";
        } else if (line.startsWith("Snk_Real")) {
            type = "real";
        } else if (line.startsWith("Snk_Strg")) {
            type = "string";
        } else {
            result.append("Error: Invalid declaration syntax. Line: ").append(line).append("\n");
            return;
        }

        String[] parts = line.substring(line.indexOf(' ')).split(",");
        for (String part : parts) {
            String identifier = part.trim().replace("#", "").trim();
            if (identifierPattern.matcher(identifier).matches()) {
                if (!variableTypes.containsKey(identifier)) {
                    variableTypes.put(identifier, type);
                    result.append("Declaration detected: ").append(identifier).append(" as ").append(type).append("\n");
                } else {
                    result.append("Error: Variable ").append(identifier).append(" is already declared. Line: ").append(line).append("\n");
                }
            } else {
                result.append("Error: Invalid identifier '").append(identifier).append("' in declaration. Line: ").append(line).append("\n");
            }
        }
    }

    private void handleAssignment(String line, StringBuilder result) {
        String[] parts = line.split(" ");
        if (parts.length < 3) {
            result.append("Error: Invalid assignment syntax. Line: ").append(line).append("\n");
            return;
        }

        String variable = parts[1];
        String value = parts[2].replace("#", "").trim();

        if (!variableTypes.containsKey(variable)) {
            result.append("Error: Variable '").append(variable).append("' is not declared before assignment. Line: ").append(line).append("\n");
            return;
        }

        String type = variableTypes.get(variable);

        if ((type.equals("int") && intPattern.matcher(value).matches()) ||
            (type.equals("real") && realPattern.matcher(value).matches()) ||
            (type.equals("string") && stringPattern.matcher(value).matches())) {
            result.append("Assignment detected: ").append(variable).append(" = ").append(value).append("\n");
        } else {
            result.append("Error: Type mismatch in assignment. Expected ").append(type).append(" but got '").append(value).append("'. Line: ").append(line).append("\n");
        }
    }

    private void handleGet(String line, StringBuilder result) {
        String[] parts = line.split(" ");
        if (parts.length < 4 || !parts[2].equals("from")) {
            result.append("Error: Invalid Get syntax. Line: ").append(line).append("\n");
            return;
        }

        String variable1 = parts[1];
        String variable2 = parts[3].replace("#", "").trim();

        if (!variableTypes.containsKey(variable1)) {
            result.append("Error: Variable '").append(variable1).append("' is not declared. Line: ").append(line).append("\n");
        }
        if (!variableTypes.containsKey(variable2)) {
            result.append("Error: Variable '").append(variable2).append("' is not declared. Line: ").append(line).append("\n");
        }

        if (variableTypes.containsKey(variable1) && variableTypes.containsKey(variable2)) {
            result.append("Get instruction detected: ").append(variable1).append(" from ").append(variable2).append("\n");
        }
    }

    private void handlePrint(String line, StringBuilder result) {
        String[] parts = line.substring(line.indexOf(' ') + 1).replace("#", "").trim().split(",");
        for (String content : parts) {
            content = content.trim();
            if (stringPattern.matcher(content).matches() || variableTypes.containsKey(content)) {
                result.append("Print instruction detected: ").append(content).append("\n");
            } else {
                result.append("Error: Invalid print content '").append(content).append("'. Line: ").append(line).append("\n");
            }
        }
    }
}
>>>>>>> feaa3dc0944469632333e83cb44484c4517191a3
