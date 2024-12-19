<script src="${pageContext.request.contextPath}/js/pdf-2.16.105.min.js"></script>

<style>

    /* Make the modal take 90% of the viewport height */
    .modal-dialog {
        display: flex;
        align-items: center; /* Vertically center modal */
        justify-content: center;
        height: 90vh; /* 90% of the viewport height */
        margin-top: 2%;
    }

    .modal-content {
        height: 90vh; /* Ensure the content takes 90% height */
        overflow: hidden; /* Prevent content overflow */
    }

    .modal-body {
        height: calc(100% - 120px); /* Adjust for header and footer height */
        overflow-y: auto; /* Enable scrolling for content */
    }

    .topic-card {
        background-color: #d1ecf1; 
        padding: 20px; 
        border-radius: 10px; 
        box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2); 
    }
    .modal-extra-large {
        max-width: 90%;
        max-height: 90%;
    }

    input[type="radio"]{
        transform: scale(2);
    }

    /* no square in check box */
    .custom-control-label::before, .custom-control-label::after {
        display: none;
    }

    .circle {
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 50%;
        width: 30px;
        height: 30px;
        border: 1px solid black;
    }

    .correct {
        color: white;
        background-color: red;
        border-color: red;
    }

    .student {
        color: white;
        background-color: blue;
        border-color: blue;
    }

    .answer {
        color: white;
        background-color: red;
        border-color: red;
    }
    
    .different {
        background-color: #FDEFB2;
    }

    .custom-badge {
        font-size: 1.0em; /* Increase font size */
        padding: 0.5em; /* Adjust padding */
        margin-bottom: 1.0em;
    }

    /* Toolbar Styling */
    /* .pdf-toolbar {
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #f1f1f1;
        padding: 10px;
        border-bottom: 1px solid #ddd;
    }
    .pdf-toolbar button {
        background-color: #007bff;
        color: white;
        border: none;
        padding: 5px 10px;
        margin: 0 5px;
        cursor: pointer;
        border-radius: 4px;
    }
    .pdf-toolbar button:disabled {
        background-color: #aaa;
        cursor: not-allowed;
    }
    .pdf-toolbar span {
        font-weight: bold;
        margin: 0 10px;
    }
    .pdfViewerContainer {
        width: 100%;
        height: auto;
        overflow: auto;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #f8f9fa;
    }
    .pdfCanvas {
        display: block;
        max-width: 100%;
        height: auto;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    } */

</style>

<script>

var pdfDoc = null;
let pageNum = 1;
let scale = 1.5;

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//          Load Practice PDF
////////////////////////////////////////////////////////////////////////////////////////////////////////////
function loadPracticePdf(pdfPath) {
    // Set the workerSrc before loading the PDF
    pdfjsLib.GlobalWorkerOptions.workerSrc = '${pageContext.request.contextPath}/js/pdf.worker-2.16.105.min.js';
    // pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.16.105/pdf.worker.min.js';


    pdfjsLib.getDocument(pdfPath).promise.then((pdf) => {
        pdfDoc = pdf;
        document.getElementById("practiceTotalPages").textContent = pdf.numPages;
        renderPracticePage(pageNum);
    });
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//          Load Answer PDF
////////////////////////////////////////////////////////////////////////////////////////////////////////////
function loadAnswerPdf(pdfPath) {
    // Set the workerSrc before loading the PDF
    pdfjsLib.GlobalWorkerOptions.workerSrc = '${pageContext.request.contextPath}/js/pdf.worker-2.16.105.min.js';
    // console.log(pdfPath);
    pdfjsLib.getDocument(pdfPath).promise.then((pdf) => {
        pdfDoc = pdf;
        document.getElementById("answerTotalPages").textContent = pdf.numPages;
        renderAnswerPage(pageNum);
    });
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//          Render Practice PDF
////////////////////////////////////////////////////////////////////////////////////////////////////////////
let renderPracticeTask = null; // Track the rendering task
function renderPracticePage(num) {
    const canvas = document.getElementById("practicePdfCanvas");
    const ctx = canvas.getContext("2d");

    // Cancel the previous render task if it exists
    if (renderPracticeTask) {
        renderPracticeTask.cancel();
    }

    pdfDoc.getPage(num).then((page) => {
        const viewport = page.getViewport({ scale });
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        const renderContext = {
            canvasContext: ctx,
            viewport: viewport,
        };

        // Render the page and track the task
        renderPracticeTask = page.render(renderContext);

        // When rendering is complete, update toolbar
        renderPracticeTask.promise.then(() => {
            document.getElementById("practiceCurrentPage").textContent = num;
            document.getElementById("practicePrevPage").disabled = num <= 1;
            document.getElementById("practiceNextPage").disabled = num >= pdfDoc.numPages;
        }).catch((err) => {
            console.log("Render cancelled:", err);
        });
    });
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//          Render Answer PDF
////////////////////////////////////////////////////////////////////////////////////////////////////////////
let renderAnswerTask = null; // Track the rendering task
function renderAnswerPage(num) {
    const canvas = document.getElementById("answerPdfCanvas");
    const ctx = canvas.getContext("2d");
    const container = document.querySelector(".pdfViewerContainer");

    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    if (renderAnswerTask) {
        renderAnswerTask.cancel();
    }

    pdfDoc.getPage(num).then((page) => {
        // Calculate container width safely
        const containerWidth = container.clientWidth || 800;
        const viewport = page.getViewport({ scale: 1 });
        const scale = containerWidth / viewport.width || 1;

        // console.log("Container width:", containerWidth);
        // console.log("Calculated scale:", scale);

        const scaledViewport = page.getViewport({ scale });
        canvas.height = scaledViewport.height;
        canvas.width = scaledViewport.width;

        const renderContext = {
            canvasContext: ctx,
            viewport: scaledViewport,
        };

        renderAnswerTask = page.render(renderContext);
        renderAnswerTask.promise.then(() => {
            document.getElementById("answerCurrentPage").textContent = num;
            document.getElementById("answerPrevPage").disabled = num <= 1;
            document.getElementById("answerNextPage").disabled = num >= pdfDoc.numPages;
            // console.log("Page rendered successfully");
        }).catch((err) => {
            console.error("Render failed:", err);
        });
    }).catch((err) => {
        console.error("Failed to load page:", err);
    });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display Material (Pdf/Answer Sheet)
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayMaterial(practiceId) {
    $.ajax({
        url: '${pageContext.request.contextPath}/connected/getPractice/' + practiceId,
        method: "GET",
        success: function (practice) {
            const pdfPath = practice.pdfPath;
            $('#practiceModal').off('shown.bs.modal'); // Remove previous modal event
            $('#practiceModal').on('shown.bs.modal', function () {
                // Render the PDF
                pageNum = 1; // Reset page
                loadPracticePdf(pdfPath);
                // Ensure event listeners are not duplicated
                document.getElementById("practicePrevPage").onclick = () => {
                    if (pageNum > 1) {
                        pageNum--;
                        renderPracticePage(pageNum);
                    }
                };
                document.getElementById("practiceNextPage").onclick = () => {
                    if (pageNum < pdfDoc.numPages) {
                        pageNum++;
                        renderPracticePage(pageNum);
                    }
                };
                // Manipulate answer sheet
                var numAnswer = practice.answerCount;
                var numQuestion = practice.questionCount; // replace with the actual property name
                var container = $('.answerSheet');
                container.empty(); // remove existing question elements
                
                // header
                var header = '<div class="h5 bg-primary" style="position: relative; display: flex; justify-content: center; align-items: center; color: #ffffff; text-align: center; margin-bottom: 20px; padding: 10px; background-color: #f8f9fa; border: 2px solid #e9ecef; border-radius: 5px;">'
                    + 'Answers&nbsp;&nbsp;<span id="chosenAnswerNum" name="chosenAnswerNum" class="text-warning" title="Student Answer">0</span>&nbsp;/&nbsp;<span id="numQuestion" name="numQuestion" title="Total Question">'+ numQuestion +'</span></div>';
                container.append(header);
                
                for (var i = 1; i <= numQuestion; i++) {
                    var questionDiv = $('<div>').addClass('mt-5 mb-4');
                    var questionLabel = $('<div>').addClass('form-check form-check-inline h5 ml-1').text(' ' + i + '. ');
                    questionLabel.css('width', '30px');
                    questionDiv.append(questionLabel);
                
                    // Determine the options to display based on numAnswer
                    var options = ['A', 'B', 'C', 'D', 'E'].slice(0, numAnswer);
                
                    options.forEach(function(option, index) {
                        var optionDiv = $('<div>').addClass('form-check form-check-inline h5 ml-1');
                        var input = $('<input>').addClass('form-check-input mr-3 ml-1').attr({
                            type: 'radio',
                            name: 'inlineRadioOptions' + i,
                            id: 'inlineRadio' + i + (index + 1), // append the question number to the id
                            value: index + 1
                        });
                        var label = $('<label>').addClass('form-check-label').attr('for', 'inlineRadio' + i + (index + 1)).text(option);
                        optionDiv.append(input, label);
                        questionDiv.append(optionDiv);
                    });
                    container.append(questionDiv);
                }

                // Add event listener to radio buttons
                $('.form-check-input').on('change', function() {
                    var chosenAnswerNum = $('input[type=radio]:checked').length;
                    $('#chosenAnswerNum').text(chosenAnswerNum);
                });
                var footer = '<div><button type="submit" class="btn btn-primary w-100" onclick="checkAnswer(' + practiceId + ', ' +  numQuestion +')">SUBMIT</button></div>';
                container.append(footer);
            });
            // console.log(practice);
            var setName = practice.volume;
            if((PRACTICE_GROUP == 1) || (PRACTICE_GROUP == 2)){
                switch (practice.volume) {
                    case 1:
                        setName = 'Vol.1-1';
                        break;
                    case 2:
                        setName = 'Vol.1-2';
                        break;
                    case 3:
                        setName = 'Vol.2-1';
                        break;
                    case 4:
                        setName = 'Vol.2-2';
                        break;
                    case 5:
                        setName = 'Vol.3-1';
                        break;
                    case 6:
                        setName = 'Vol.3-2';
                        break;
                    case 7:
                        setName = 'Vol.4-1';
                        break;
                    case 8:
                        setName = 'Vol.4-2';
                        break;
                    case 9:
                        setName = 'Vol.5-1';
                        break;
                    case 10:
                        setName = 'Vol.5-2';
                        break;
                }
            } 
            // Open the modal
            document.getElementById("practiceModalLabel").innerHTML = practice.title + ' Practice - Set <span class="text-warning">' + setName + "</span>";
            $('#practiceModal').modal('show');
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log("Error: " + errorThrown);
        },
    });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display Answer (Video/Pdf)
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayAnswer(practiceId, title, week) {
    $.ajax({
        url : '${pageContext.request.contextPath}/connected/practiceAnswer/' + studentId + '/' + practiceId,
        method: "GET",
        success: function(value) {
            console.log(value);
            $('#answerModal').off('shown.bs.modal'); // Remove previous modal event
            $('#answerModal').on('shown.bs.modal', function () {
                // Video player
                var videoPlayer = document.getElementById("answerVideoPlayer");
                videoPlayer.src = value.videoPath;
                // Render the PDF
                pageNum = 1; // Reset page
                loadAnswerPdf(value.pdfPath);
                // Ensure event listeners are not duplicated
                document.getElementById("answerPrevPage").onclick = () => {
                    if (pageNum > 1) {
                        pageNum--;
                        renderAnswerPage(pageNum);
                    }
                };
                document.getElementById("answerNextPage").onclick = () => {
                    if (pageNum < pdfDoc.numPages) {
                        pageNum++;
                        renderAnswerPage(pageNum);
                    }
                };
                // manipulate answer sheet
                var answerNumQuestion = value.answers.length;
                var answerCount = value.answerCount;
                var result = calculateScore(value.students, value.answers);
                var score = result.score;
                var countCorrect = result.numCorrect;    
                var container = $('.resultSheet');
                container.empty(); // remove existing question elements

                // header
                var header = '<div id="stickyHeader" class="h5" style="position: relative; display: flex; justify-content: center; align-items: center; color: #333; text-align: center; margin-bottom: 20px; padding: 10px; background-color: #f8f9fa; border: 2px solid #e9ecef; border-radius: 5px;">'
                    + '<button onclick="confirmRetake(' + value.practiceId + ')" style="position: absolute; left: 20px; padding: 5px 10px; background-color: #007bff; color: #fff; border: none; border-radius: 5px; cursor: pointer;"><i class="bi bi-arrow-clockwise"></i>&nbsp;Retake</button>' 
                    + 'My Score : ' + score + '% (<span id="correctAnswerNum" name="correctAnswerNum" style="color:blue;" title="Student Answer">' + countCorrect + '</span>/<span id="answerNumQuestion" name="answerNumQuestion" style="color:red;" title="Correct Answer">'+ (answerNumQuestion-1) +'</span>)</div>';
                container.append(header);

                for (var i = 1; i < answerNumQuestion; i++) {
                    var questionDiv = $('<div>').addClass('m-4');
                    var questionLabel = $('<div>').addClass('form-check form-check-inline h6 ml-5').text(' ' + i + '. ');
                    // Set a consistent width for the question label container
                    questionLabel.css('width', '50px'); // Adjust the width as needed
                    questionDiv.append(questionLabel);

                    // Determine the options to display based on answerCount
                    var options = ['A', 'B', 'C', 'D', 'E'].slice(0, answerCount);

                    options.forEach(function (option, index) {
                        var optionDiv = $('<div>').addClass('custom-control custom-control-inline h6');
                        var label = $('<label>').addClass('custom-control-label circle').attr('for', 'customCheck' + i + (index + 1)).text(option);
                        if (value.students[i] == index + 1 && value.answers[i] == index + 1) {
                            // If student's answer and correct answer are the same, add 'correct' class
                            label.addClass('correct');
                        } else if (value.students[i] == index + 1) {
                            // If only student's answer is this option, add 'student' class
                            label.addClass('student');
                        } else if (value.answers[i] == index + 1) {
                            // If only correct answer is this option, add 'answer' class
                            label.addClass('answer');
                        }
                        if (value.students[i] != value.answers[i]) {
                            // If student's answer and correct answer are different, add 'different' class to the question div
                            questionDiv.addClass('different');
                        }
                        optionDiv.append(label);
                        questionDiv.append(optionDiv);
                    });
                    container.append(questionDiv);    
                }
            });
            // Open the modal
            document.getElementById("practiceAnswerModalLabel").innerHTML = title + ' Practice - Set <span class="text-warning">' + week + "</span>";
            $('#answerModal').modal('show');
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });   
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Confirm take the practice again
////////////////////////////////////////////////////////////////////////////////////////////////////////
function confirmRetake(practiceId) {
    // Show the warning modal
    $('#practiceWarningModal').modal('show');

    // Attach the click event handler to the "I agree" button
    $('#agreePracticeWarning').one('click', function() {
        retestRequest(practiceId);
        $('#practiceWarningModal').modal('hide');
    });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Submit Answer
////////////////////////////////////////////////////////////////////////////////////////////////////////
function checkAnswer(practiceId, numQuestion) {
    // Collect all the selected answers
    var answers = [];
    for (var i = 1; i <= numQuestion; i++) {
        var selectedOption = $('input[name=inlineRadioOptions' + i + ']:checked').val();
        var answer = parseInt(selectedOption) || 0; // Convert to integer and default to 0 if NaN
        answers.push({
            question: i,
            answer: answer
        });
    }
    //Make an AJAX call to send the data to the server
    $.ajax({
        url: '${pageContext.request.contextPath}/connected/addStudentPractice',
        method: 'POST',
        data: JSON.stringify({
            studentId : studentId,
            practiceId : practiceId,
            answers : answers
        }),
        contentType: 'application/json',
        success: function(response) {
             // pdf & answer sheet dialogue disappears
            $('#practiceModal').modal('hide');
            $('#success-alert .modal-body').html('Answer is successfully submitted.');
	        $('#success-alert').modal('show');

			// Attach an event listener to the success alert close event
			$('#success-alert').on('hidden.bs.modal', function () {
				// Reload the page after the success alert is closed
				location.href = window.location.pathname; // Passing true forces a reload from the server and not from the cache
			});

        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Re-test
////////////////////////////////////////////////////////////////////////////////////////////////////////
function retestRequest(practiceId) {
    //Make an AJAX call to send the data to the server
    $.ajax({
        url: '${pageContext.request.contextPath}/connected/deleteStudentPractice/' + studentId + '/' + practiceId,
        method: 'DELETE',
        success: function(response) {
             // pdf & answer sheet dialogue disappears
             $('#practiceModal').modal('hide');
            location.href = window.location.pathname; // Passing true forces a reload from the server and not from the cache
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });
}

//////////////////////////////////////////////////////////////////
// Calculate score by comparing student answers and answer sheet
//////////////////////////////////////////////////////////////////
function calculateScore(studentAnswers, answerSheet) {
    // Check if both arrays have the same length
    if (!studentAnswers || !answerSheet || studentAnswers.length !== answerSheet.length) {
        return 0;
    }
    var totalQuestions = answerSheet[0]; // Assuming the first element is the total count

    // Iterate through the arrays and compare corresponding elements
    var correctAnswers = 0;
    for (var i = 1; i <= totalQuestions; i++) {
        var studentAnswer = studentAnswers[i];
        var correctAnswer = answerSheet[i];

        if (studentAnswer === correctAnswer) {
            correctAnswers++;
        }
    }
    // Calculate the final score as a percentage
    var score = (correctAnswers / totalQuestions) * 100;
    //var rounded = Math.round(score * 100) / 100;
    var rounded = Math.round(score);
    // return rounded;
    return {numCorrect: correctAnswers, score : rounded};
}

</script>

<!-- Pop up Practice modal -->
<div class="modal fade" id="practiceModal" tabindex="-1" role="dialog" aria-labelledby="practiceModalLabel" aria-hidden="true"  data-backdrop="static" data-keyboard="false">
    <div class="modal-dialog modal-extra-large" role="document">
        <div class="modal-content" style="height: 90vh;">
            <div class="modal-header bg-primary text-white text-center">
                <h5 class="modal-title w-100" id="practiceModalLabel"></h5>
                <button type="button" class="close position-absolute" style="right: 1rem;" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>            
            <div class="modal-body bg-light">
                <div class="row">
                    <div class="col-md-9 bg-white p-1 border">
                        <div class="pdf-toolbar">
                            <button id="practicePrevPage">Previous</button>
                                <span>Page: <span id="practiceCurrentPage">1</span> / <span id="practiceTotalPages">1</span></span>
                            <button id="practiceNextPage">Next</button>
                        </div>
                        <div class="pdfViewerContainer">
                            <canvas id="practicePdfCanvas" class="pdfCanvas"></canvas>
                        </div>
                    </div>
                    <div class="col-md-3 bg-white p-1 border" style="height: 85vh;">
                        <div style="display: flex; flex-direction: column; height: 100%;">
                            <!-- ANSWER SHEET -->
                            <div class="answerSheet" style="overflow-y: auto; flex-grow: 1;"></div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer bg-dark text-white">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<!-- Pop up Answer modal -->
<div class="modal fade" id="answerModal" tabindex="-1" role="dialog" aria-labelledby="practiceAnswerModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-extra-large" role="document">
        <div class="modal-content" style="height: 90vh;">
            <div class="modal-header bg-primary text-white text-center">
                <h5 class="modal-title w-100" id="practiceAnswerModalLabel"></h5>
                <button type="button" class="close position-absolute" style="right: 1rem;" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body bg-light">
                <div class="row">
                    <div class="col-md-6 d-flex flex-column justify-content-center bg-white p-1 border">
                        <div style="display: flex; flex-direction: column; height: 80vh;">
                            <video id="answerVideoPlayer" controls controlsList="nodownload" style="flex: 6;">
                                <source src="" type="video/mp4">
                            </video>
                            <div style="overflow-y: auto; flex: 1;"></div>
                            <div class="resultSheet" style="overflow-y: auto; flex: 3;">
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6 bg-white p-1 border">
                        <div class="pdf-toolbar">
                            <button id="answerPrevPage">Previous</button>
                                <span>Page: <span id="answerCurrentPage">1</span> / <span id="answerTotalPages">1</span></span>
                            <button id="answerNextPage">Next</button>
                        </div>
                        <div class="pdfViewerContainer">
                            <canvas id="answerPdfCanvas" class="pdfCanvas"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer bg-dark text-white">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!--Practice Warning Modal -->
<div class="modal fade" id="practiceWarningModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content" style="border: 2px solid #ffc107; border-radius: 10px;">
            <div class="modal-header bg-warning" style="display: block;">
                <p style="text-align: center; margin-bottom: 0;"><span style="font-size:18px"><strong>Practice for James An College Class</strong></span></p>
            </div>
            <div class="modal-body" style="background-color: #f8f9fa; border-radius: 5px; padding: 20px;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <img src="${pageContext.request.contextPath}/image/retake.png" style="width: 150px; height: 150px; border-radius: 5%;">
                </div>
                <span class="text-primary"><strong>Retake Practice</strong></span>
                        Feel free to practice as many times as you'd like! Just remember, each time you retake a practice, your old result will be replaced by the new one.
                
                <br><br><p class="text-center"><strong>Good luck with your practice!</strong></p>      
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="agreePracticeWarning">I understand</button>
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>