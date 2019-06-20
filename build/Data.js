const fs = require("fs");
const data = require("./Data.json");

const {rules, translations} = transformData(data);

writeRules(rules);
writeTranslations(translations);

function transformData(data) {
    let nextId = 0;
    let rules = [];
    let translations = [];

    processNode(data);

    return {
        rules: rules.join("\n"),
        translations: translations.join("\n")
    };

    function processNode(node, parentQuestionId = "start", parentAnswerId) {
        const nodeId = nextId++;
        const condition = `${parentQuestionId}${parentAnswerId ? ` ${parentAnswerId}` : ""}`;

        if (node.answers) {
            const questionId = `question-${nodeId}`;

            const answerIds = Object.keys(node.answers)
                .map((answerKey, index) => {
                    const answerId = `${questionId}-answer-${index}`;
                    translations.push(`${answerId}=${answerKey}`);

                    const nextQuestion = node.answers[answerKey];
                    processNode(nextQuestion, questionId, answerId);

                    return answerId;
                });

            rules.push(`
                (defrule ${questionId} ""
                    (logical (${condition}))
                    =>
                    (assert
                        (UI-state
                            (display ${questionId})
                            (relation-asserted ${questionId})
                            (response ${answerIds[0]})
                            (valid-answers ${answerIds.join(" ")})
                        )
                   )
                )
            `);

            translations.push(`${questionId}=${node.text}`);
        } else {
            const endingId = `ending-${nodeId}`;

            rules.push(`
                (defrule ${endingId} ""
                    (logical (${condition}))
                    =>
                    (assert
                        (UI-state
                            (display ${endingId})
                            (state final)
                        )
                    )
                )
            `);

            translations.push(`${endingId}=${node.text}`);
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
