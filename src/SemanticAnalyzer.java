package snake;

import java.util.HashMap;
import java.util.Map;

public class SemanticAnalyzer {
    private Map<String, String> declaredVariables = new HashMap<>();
    private Map<String, Object> variableValues = new HashMap<>();

    public String analyze(String code) {
        StringBuilder result = new StringBuilder("Résultat de l'analyse sémantique :\n");
        String[] lines = code.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Ignorer les lignes vides
            if (trimmedLine.isEmpty()) continue;

            // Ignorer les lignes spécifiques
            if (trimmedLine.startsWith("Snk_Begin") || trimmedLine.startsWith("Snk_End") 
                || trimmedLine.startsWith("If") || trimmedLine.startsWith("Else")
                || trimmedLine.startsWith("Begin") || trimmedLine.startsWith("End")
                || trimmedLine.startsWith("##")) {
                continue;
            }

            // Gestion des déclarations
            if (trimmedLine.startsWith("Snk_Int") || trimmedLine.startsWith("Snk_Real") || trimmedLine.startsWith("Snk_Strg")) {
                processDeclaration(trimmedLine, result);
                continue;
            }

            // Gestion des affectations
            if (trimmedLine.startsWith("Set")) {
                processAssignment(trimmedLine, result);
                continue;
            }

            // Gestion des instructions Get
            if (trimmedLine.startsWith("Get")) {
                processGet(trimmedLine, result);
                continue;
            }

            // Gestion des affichages (Snk_Print)
            if (trimmedLine.startsWith("Snk_Print")) {
                processPrint(trimmedLine, result);
                continue;
            }

            // Gestion des expressions arithmétiques
            if (isArithmeticExpression(trimmedLine)) {
                processArithmetic(trimmedLine, result);
                continue;
            }

            // Lignes non reconnues
            result.append("Erreur : Ligne non reconnue ou syntaxe incorrecte : ").append(trimmedLine).append("\n");
        }

        return result.toString();
    }

    private void processDeclaration(String line, StringBuilder result) {
        String type = line.split(" ")[0].replace("Snk_", "").toLowerCase();
        String variablesPart = line.substring(line.indexOf(' ') + 1).replace("#", "");
        String[] variables = variablesPart.split(",");

        for (String variable : variables) {
            variable = variable.trim();
            if (!variable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                result.append("Erreur : Identifiant invalide : ").append(variable).append("\n");
            } else if (declaredVariables.containsKey(variable)) {
                result.append("Erreur : La variable '").append(variable).append("' est déjà déclarée.\n");
            } else {
                declaredVariables.put(variable, type);
                result.append("Déclaration sémantique correcte : ").append(variable).append(" de type ").append(type).append("\n");
            }
        }
    }

    private void processAssignment(String line, StringBuilder result) {
        String[] parts = line.split("\\s+", 3);
        if (parts.length < 3 || !line.endsWith("#")) {
            result.append("Erreur : Affectation incorrecte. Ligne : ").append(line).append("\n");
            return;
        }

        String variable = parts[1];
        String expression = parts[2].replace("#", "").trim();  // Expression arithmétique

        if (!declaredVariables.containsKey(variable)) {
            result.append("Erreur : La variable '").append(variable).append("' n'est pas déclarée.\n");
            return;
        }

        String expectedType = declaredVariables.get(variable);

        // Vérification si l'expression est arithmétique
        if (isArithmeticExpression(expression)) {
            try {
                // Évaluer l'expression arithmétique
                double resultValue = evaluateExpression(expression);

                // Convertir selon le type attendu
                Object finalValue = (expectedType.equals("int")) ? (int) resultValue : resultValue;

                // Mettre à jour la valeur de la variable
                variableValues.put(variable, finalValue);
                result.append("Affectation correcte : ").append(variable).append(" = ").append(finalValue)
                      .append(" (type : ").append(expectedType).append(")\n");
            } catch (Exception e) {
                result.append("Erreur : Expression arithmétique invalide dans l'affectation : ").append(line).append("\n");
            }
        } else {
            // Si ce n'est pas une expression arithmétique, vérifier la compatibilité des types
            if (!isValueCompatible(expectedType, expression)) {
                result.append("Erreur : Incompatibilité de type pour la variable '").append(variable)
                      .append("'. Attendu : ").append(expectedType).append(". Reçu : ").append(expression).append(" (type : ").append(getValueType(expression)).append(")\n");
            } else {
                Object parsedValue = parseValue(expression, expectedType);
                variableValues.put(variable, parsedValue);
                result.append("Affectation correcte : ").append(variable).append(" = ").append(parsedValue)
                      .append(" (type : ").append(expectedType).append(")\n");
            }
        }
    }

    private void processGet(String line, StringBuilder result) {
        String[] parts = line.split("\\s+");
        if (parts.length < 4 || !"from".equals(parts[2]) || !line.endsWith("#")) {
            result.append("Erreur : Instruction Get incorrecte. Ligne : ").append(line).append("\n");
            return;
        }

        String var1 = parts[1];
        String var2 = parts[3].replace("#", "");

        if (!declaredVariables.containsKey(var1) || !declaredVariables.containsKey(var2)) {
            result.append("Erreur : Variables utilisées non déclarées dans Get. Ligne : ").append(line).append("\n");
        } else {
            result.append("Instruction Get sémantiquement correcte : ").append(line).append("\n");
        }
    }

    private void processPrint(String line, StringBuilder result) {
        String content = line.substring(line.indexOf(" ") + 1).replace("#", "").trim();

        if (content.startsWith("\"") && content.endsWith("\"")) {
            result.append("Affichage de chaîne détecté : ").append(content).append("\n");
        } else {
            String[] variables = content.split(",");
            boolean allValid = true;

            for (String variable : variables) {
                variable = variable.trim();
                if (!declaredVariables.containsKey(variable)) {
                    result.append("Erreur : La variable '").append(variable).append("' n'est pas déclarée.\n");
                    allValid = false;
                } else {
                    Object value = variableValues.getOrDefault(variable, "non initialisé");
                    result.append(variable).append(" = ").append(value).append(" ");
                }
            }

            if (allValid) result.append("\n");
        }
    }

    private void processArithmetic(String line, StringBuilder result) {
        try {
            String expression = line.replace("#", "").trim();
            double resultValue = evaluateExpression(expression);
            result.append("Résultat de l'expression : ").append(expression).append(" = ").append(resultValue).append(" (type : real)\n");
        } catch (Exception e) {
            result.append("Erreur : Expression arithmétique invalide : ").append(line).append("\n");
        }
    }
    
    
    private boolean isArithmeticExpression(String expression) {
        // Modifié pour accepter les opérations arithmétiques simples
        return expression.matches(".*[\\d\\+\\-\\*/\\^()]+.*#");
    }
   

    private double evaluateExpression(String expression) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
                return x;
            }
        }.parse();
    }
    private boolean isValueCompatible(String expectedType, String value) {
        try {
            switch (expectedType) {
                case "int":
                    Integer.parseInt(value);
                    break;
                case "real":
                    Double.parseDouble(value);
                    break;
                case "strg":
                    if (!(value.startsWith("\"") && value.endsWith("\""))) return false;
                    break;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private String getValueType(String value) {
        try {
            Integer.parseInt(value);
            return "int";
        } catch (NumberFormatException e1) {
            try {
                Double.parseDouble(value);
                return "real";
            } catch (NumberFormatException e2) {
                if (value.startsWith("\"") && value.endsWith("\"")) return "strg";
                return "unknown";
            }
        }
    }

    private Object parseValue(String value, String type) {
        switch (type) {
            case "int":
                return Integer.parseInt(value);
            case "real":
                return Double.parseDouble(value);
            case "strg":
                return value.substring(1, value.length() - 1);
            default:
                return null;
        }
    }
}