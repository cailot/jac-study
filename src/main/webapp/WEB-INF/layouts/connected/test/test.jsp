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
        height: 75vh; /* Ensure the content takes 90% height */
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
        font-size: 1.0em;
        padding: 0.5em;
        margin-bottom: 1.0em;
    }

    .pdfViewerContainer,
        .col-md-9,
        .col-md-3,
        .modal-body > .row {
            height: 100%;
    }

    .answerSheet {
        overflow-y: auto;
        flex-grow: 1;
        min-height: 0;
    }

    canvas.pdfCanvas {
        margin-top: 10px;
    }

    #testPdfCanvas {
        display: block;
        max-width: 100%;
        height: auto;
        margin-top: 300px;
        position: relative !important;
    }

</style>

<script>

var pdfDoc = null;
let pageNum = 1;
let scale = 1.5;

window.showWarning = function(id, title) {
    // Show the warning modal
    $('#testWarningModal').modal('show');
    // Attach the click event handler to the "I agree" button
    $('#agreeTestWarning').one('click', function() {
        displayMaterial(id, title);
        $('#testWarningModal').modal('hide');
    });
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//          Load Test PDF
////////////////////////////////////////////////////////////////////////////////////////////////////////////
function loadTestPdf(pdfPath) {
    // Set the workerSrc before loading the PDF
    pdfjsLib.GlobalWorkerOptions.workerSrc = '${pageContext.request.contextPath}/js/pdf.worker-2.16.105.min.js';
    
    pdfjsLib.getDocument(pdfPath).promise.then((pdf) => {
        pdfDoc = pdf;
        document.getElementById("testTotalPages").textContent = pdf.numPages;
        renderTestPage(pageNum);
    });
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//          Render Test PDF
////////////////////////////////////////////////////////////////////////////////////////////////////////////
function renderTestPage(num) {
    const canvas = document.getElementById("testPdfCanvas");
    const ctx = canvas.getContext("2d");

    // Clear the canvas to ensure previous rendering does not overlap
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    pdfDoc.getPage(num).then((page) => {
        const viewport = page.getViewport({ scale: scale });
        canvas.height = viewport.height;
        canvas.width = viewport.width;

        const renderContext = {
            canvasContext: ctx,
            viewport: viewport,
        };

        // Render the page
        page.render(renderContext).promise.then(() => {
            document.getElementById("testCurrentPage").textContent = num;
            document.getElementById("testPrevPage").disabled = num <= 1;
            document.getElementById("testNextPage").disabled = num >= pdfDoc.numPages;
        }).catch((err) => {
            console.log("Error rendering page:", err);
        });
    }).catch((err) => {
        console.log("Error loading page:", err);
    });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display Material (Pdf/Answer Sheet)
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayMaterial(testId) {
    $.ajax({
        url: '${pageContext.request.contextPath}/connected/getTest/' + testId,
        method: "GET",
        success: function (test) {
            console.log(test);
            const pdfPath = test.pdfPath;
            $('#testModal').off('shown.bs.modal'); // Remove previous modal event

            $('#testModal').on('shown.bs.modal', function () {
                    
                // Start timer 30 mins    
                console.log('Test modal is shown');
                var time = 30 * 60, // 30 minutes in seconds
                display = document.querySelector('#timerText');
                // Clear any existing interval
                if (window.timerInterval) {
                    clearInterval(window.timerInterval);
                }
                startTimer(time, display);
                
                // Render the PDF
                pageNum = 1; // Reset page
                loadTestPdf(pdfPath);
                // Ensure event listeners are not duplicated
                document.getElementById("testPrevPage").onclick = () => {
                    if (pageNum > 1) {
                        pageNum--;
                        renderTestPage(pageNum);
                    }
                };
                document.getElementById("testNextPage").onclick = () => {
                    if (pageNum < pdfDoc.numPages) {
                        pageNum++;
                        renderTestPage(pageNum);
                    }
                };
                document.getElementById("testZoomIn").onclick = () => {
                    scale += 0.1;
                    // console.log('Zoom In: ', scale);
                    renderTestPage(pageNum);
                };
                document.getElementById("testZoomOut").onclick = () => {
                    if (scale > 0.1) {
                        scale -= 0.1;
                        // console.log('Zoom Out: ', scale);
                        renderTestPage(pageNum);
                    }
                };

                // Manipulate answer sheet
                var numAnswer = test.answerCount;
                var numQuestion = test.questionCount; // replace with the actual property name
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
                var footer = '<div><button type="submit" class="btn btn-primary w-100" onclick="checkAnswer(' + testId + ', ' +  numQuestion +')">SUBMIT</button></div>';
                container.append(footer);
            });
            // console.log(practice);
            var setName = test.volume;
            if((TEST_GROUP == 1) || (TEST_GROUP == 2)){
                switch (test.volume) {
                    case 1:
                        setName = 'Vol.1';
                        break;
                    case 2:
                        setName = 'Vol.2';
                        break;
                    case 3:
                        setName = 'Vol.3';
                        break;
                    case 4:
                        setName = 'Vol.4';
                        break;
                    case 5:
                        setName = 'Vol.5';
                        break;
                }
            } 
            // Open the modal
            document.getElementById("testModalLabel").innerHTML = test.name + ' Test - Set <span class="text-warning">' + setName + "</span>";
            $('#testModal').modal('show');
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log("Error: " + errorThrown);
        },
    });
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display Already Taken Dialog
////////////////////////////////////////////////////////////////////////////////////////////////////////
function alreadyTaken(testId, title) {
    console.log('Already taken test' + testId);
    $.ajax({
        url : '${pageContext.request.contextPath}/connected/studentTestDate/' + studentId + '/' + testId,
        method: "GET",
        success: function(test) {
            document.getElementById("alreadyTitle").innerHTML = title; 
            document.getElementById("alreadyDate").innerHTML = test; 
            $('#takenWarningModal').modal('show');
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });   
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Submit Answer
////////////////////////////////////////////////////////////////////////////////////////////////////////
function checkAnswer(testId, numQuestion) {
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
        url: '${pageContext.request.contextPath}/connected/addStudentTest',
        method: 'POST',
        data: JSON.stringify({
            studentId : studentId,
            testId : testId,
            answers : answers
        }),
        contentType: 'application/json',
        success: function(response) {
            // pdf & answer sheet dialogue disappears
            $('#testModal').modal('hide');
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

</script>

<!-- Pop up Test modal -->
<div class="modal fade" id="testModal" tabindex="-1" role="dialog" aria-labelledby="testModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
    <div class="modal-dialog modal-extra-large" role="document">
        <div class="modal-content" style="height: 85vh;">
            <div class="modal-header bg-primary text-white text-center">
                <h5 class="modal-title w-100" id="testModalLabel"></h5>
                <button type="button" class="close position-absolute" style="right: 1rem;" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>

            <div class="modal-body bg-light p-0" style="height: 100%;">
                <div class="row m-0" style="height: 100%;">
                    
                    <!-- PDF LEFT PANEL -->
                    <div class="col-md-9 bg-white border d-flex flex-column" style="height: 100%;">
                        <!-- PDF Toolbar -->
                        <div class="pdf-toolbar p-2 border-bottom" style="flex-shrink: 0;">
                            <button id="testPrevPage">Previous</button>
                            <span>Page: <span id="testCurrentPage">1</span> / <span id="testTotalPages">1</span></span>
                            <button id="testNextPage">Next</button>
                            <button id="testZoomOut">-</button>
                            <button id="testZoomIn">+</button>
                        </div>
                        <!-- PDF Viewer Canvas -->
                        <div class="pdfViewerContainer flex-grow-1 overflow-auto">
                            <canvas id="testPdfCanvas" class="pdfCanvas" style="display: block; max-width: 100%; height: auto;"></canvas>
                        </div>
                    </div>
                    <!-- ANSWER SHEET PANEL -->
                    <div class="col-md-3 bg-white border d-flex flex-column p-3" style="height: 100%;">
                        <!-- Timer -->
                        <div id="timer" class="text-center mb-2" style="font-size: 20px; font-weight: bold;">
                            <i class="bi bi-stopwatch"></i>&nbsp;&nbsp;<span id="timerText"></span>
                        </div>
                        <!-- Answer Sheet (scrollable) -->
                        <div class="answerSheet flex-grow-1 overflow-auto" style="min-height: 0;"></div>
                    </div>

                </div>
            </div>

            <div class="modal-footer bg-dark text-white">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>


<!--Test Warning Modal -->
<div class="modal fade" id="testWarningModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content" style="border: 2px solid #ffc107; border-radius: 10px;">
            <div class="modal-header bg-warning" style="display: block;">
                <p style="text-align: center; margin-bottom: 0;"><span style="font-size:18px"><strong>Test Instruction for James An College Class</strong></span></p>
            </div>
            <div class="modal-body" style="background-color: #f8f9fa; border-radius: 5px; padding: 20px;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <img src="${pageContext.request.contextPath}/image/test.png" style="width: 150px; height: 150px; border-radius: 5%;">
                </div>
                <!-- Add your warning message or content here -->
                <ol style="line-height: 1.6;">
                    <li><span class="text-primary"><strong>Test Duration</strong></span>
                        Ensure completion within the 30-minute time limit provided for the test.
                    </li>
                    <li><span class="text-primary"><strong>Single Attempt</strong></span>
                        Each student has a single opportunity to attempt the test, and once initiated, retakes are not permitted.
                    </li>
                    <li><span class="text-primary"><strong>Submission</strong></span>
                        Upon finishing the test, submit your answers using the "Submit" button; changes cannot be made thereafter.
                    </li>
                    <!-- <li><span class="text-primary"><strong>Feedback</strong></span>
                        Instantly view both your answers and the correct ones for each question immediately after submission, facilitating review and learning from mistakes.
                    </li> -->
                    <li><span class="text-primary"><strong>Test Results</strong></span>
                        Access detailed reports, including individual answers and class statistics providing insights into your performance relative to peers, under the 'Test Result' menu later.
                    </li>
                </ol>
                <br><br>
                <p><strong>Please adhere to these guidelines to ensure a fair and effective assessment process. Good luck with your test!</strong></p>      
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="agreeTestWarning">I understand</button>
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- Already Taken Warning Modal -->
<div class="modal fade" id="takenWarningModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content" style="border: 2px solid #ffc107; border-radius: 10px; height: 50vh;">
            <div class="modal-header bg-warning" style="display: block;">
                <p style="text-align: center; margin-bottom: 0;"><span style="font-size:18px"><strong>You have already taken this test !</strong></span></p>
            </div>
            <div class="modal-body" style="background-color: #f8f9fa; border-radius: 5px; padding: 20px;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <img src="${pageContext.request.contextPath}/image/test-done.png" style="width: 150px; height: 150px; border-radius: 5%;">
                </div>
                <strong><span class="text-primary" id="alreadyTitle">Retake Practice</span></strong>
                    Our system has detected that you have already taken this test at <strong><span id="alreadyDate"></span></strong>.
                <br><br><p class="text-center"><strong>You will be able to check your results<br> once the system is ready</strong></p>      
            </div>
            <div class="modal-footer">
                <!-- <button type="button" class="btn btn-primary" id="agreePracticeWarning">I understand</button> -->
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>


<script>
    $(document).ready(function() {
        // stop the timer when the modal is hidden
        $('#testModal').on('hidden.bs.modal', function () {
            console.log('Test modal is hidden');
            var display = document.querySelector('#timerText');
            display.textContent = "";
            // Clear the interval to stop the timer
            if (window.timerInterval) {
                clearInterval(window.timerInterval);
                window.timerInterval = null;
            }
        });
    });

    function startTimer(duration, display) {
        var timer = duration, minutes, seconds;
        display.textContent = ""; // Clear the timer display at the start of the function
        window.timerInterval = setInterval(function () {
            minutes = parseInt(timer / 60, 10);
            seconds = parseInt(timer % 60, 10);

            minutes = minutes < 10 ? "0" + minutes : minutes;
            seconds = seconds < 10 ? "0" + seconds : seconds;
            console.log(minutes + ":" + seconds);
            display.textContent = minutes + ":" + seconds;

            if (--timer < 0) {
                clearInterval(window.timerInterval);
                display.textContent = "TIME'S UP";
            }
        }, 1000);
    }
</script>