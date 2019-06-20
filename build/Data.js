const fs = require("fs");
const data = require("./Data.json");

const {rules, translations} = transformData(data);

writeRules(rules);
writeTranslations(translations);

function transformData(data) {
    let id = 0;
    let rules = [];
    let translations = [];

    processNode(data);

    return {
        rules: rules.join("\n"),
        translations: translations.join("\n")
    };

    function processNode(node, parentId = "start", parentAnswer) {
        const nodeId = node.id || id++;

        if (node.answers) {
            const possibleAnswers = Object.keys(node.answers);
            const questionId = `question-${nodeId}`;

            rules.push(`
                (defrule ${questionId} ""
                    (logical (${parentId}${parentAnswer ? ` ${parentAnswer}` : ""}))
                    =>
                    (assert
                        (UI-state
                            (display ${questionId})
                            (relation-asserted ${questionId})
                            (response ${possibleAnswers[0]})
                            (valid-answers ${possibleAnswers.join(" ")})
                        )
                   )
                )
            `);

            translations.push(`${questionId}=${node.text}`);

            for (const answer in node.answers) {
                if (node.answers.hasOwnProperty(answer)) {
                    const nextQuestion = node.answers[answer];
                    processNode(nextQuestion, questionId, answer)
                }
            }
        } else {
            const answerId = `answer-${nodeId}`;

            rules.push(`
                (defrule ${answerId} ""
                    (logical (${parentId}${parentAnswer ? ` ${parentAnswer}` : ""}))
                    =>
                    (assert
                        (UI-state
                            (display ${answerId})
                            (state final)
                        )
                    )
                )
            `);

            translations.push(`answer-${nodeId}=${node.text}`);
        }
    }
}

function writeRules(rules) {
    const defaultRules = fs.readFileSync("./Horror.clp").toString();
    fs.writeFile("../resources/Horror.clp", defaultRules + rules, errorHandler);
}

function writeTranslations() {
    const defaultTranslations = fs.readFileSync("./Horror.properties").toString();
    fs.writeFile("../resources/Horror.properties", defaultTranslations + translations, errorHandler);
}

function errorHandler(error) {
    if (error) {
        throw error;
    }
}
