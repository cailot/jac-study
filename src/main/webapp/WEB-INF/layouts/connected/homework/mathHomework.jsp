<style>
    .english-homework {
        background-color: #d1ecf1; 
        padding: 20px; 
        border-radius: 10px; 
        box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2); 
    }
    .modal-extra-large {
        max-width: 90%;
        max-height: 90%;
    }
</style>
<script>

const SUBJECT = 2; // 2 is Math 
var weeksData = [];

$(function() {
    $.ajax({
        url : '${pageContext.request.contextPath}/class/academy',
        method: "GET",
        success: function(response) {
            // save the response into the variable
            academicYear = response[0];
            academicWeek = parseInt(response[1]);
            //console.log('NumericGrade ---> ' + numericGrade);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });

    // get week info
    $.ajax({
        url : '${pageContext.request.contextPath}/connected/subjectList/' + SUBJECT + "/" + numericGrade + "/" + studentId,
        method: "GET",
        success: function(response) {
            // save the response into the variable
            // console.log(response);
            weeksData = response;
            displayCards();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });
});

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display Material (Video/Pdf)
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayMaterial(homeworkId) {
    var year = document.getElementById("academicYear").value;
    $.ajax({
        url : '${pageContext.request.contextPath}/connected/homework/' + homeworkId,
        method: "GET",
        success: function(value) {
            // console.log(value);
            document.getElementById("dialogSet").innerHTML = value.week;  
            // Add this part for displaying played percentage
            var videoPlayer = document.getElementById("videoPlayer");
            videoPlayer.src = value.videoPath;
            document.getElementById("pdfViewer").data = value.pdfPath;

            var progressPercentage = document.getElementById(homeworkId+"Percentage");
            var progressBar = document.getElementById(homeworkId+"Bar");
            // Define the event listener function
            var updateProgressBar = function() {
                var playedPercentage = Math.round((videoPlayer.currentTime / videoPlayer.duration) * 100);
                if(!playedPercentage || isNaN(playedPercentage)){
                    progressPercentage.innerHTML = "0%";
                    progressBar.style.width = "0%";
                } else {
                    progressPercentage.innerHTML = playedPercentage + "%";
                    progressBar.style.width = playedPercentage + "%";
                    progressBar.className = getProgressBarClass(playedPercentage);
                }
            }

            // Add the event listener when the video starts playing
            videoPlayer.addEventListener('timeupdate', updateProgressBar);

            var updateCalled = false;

            function updateProgress() {
                if (!updateCalled) {
                    updateCalled = true;
                    updateProgressOnServer(homeworkId, progressPercentage.innerHTML);
                }
            }

            videoPlayer.addEventListener("ended", updateProgress);

            // Remove the event listener when the modal is closed
            $('#homeworkModal').on('hidden.bs.modal', function () {
                videoPlayer.removeEventListener('timeupdate', updateProgressBar);
                updateProgress();
            });

            // Update progress on the server when the user navigates away or closes the browser
            window.addEventListener('beforeunload', updateProgress);

            // Show the modal
            $('#homeworkModal').modal('show');

        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });  
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Create Card
////////////////////////////////////////////////////////////////////////////////////////////////////////
function createCard(weekData) {
    // console.log(weekData);
    // console.log(weekData.week);
    const card = document.createElement('div');
    card.className = 'col-md-6';
    card.innerHTML = `
        <div class="card-body mx-auto" style="cursor: pointer; max-width: 75%;" onclick="displayMaterial(` + weekData.id + `)">
            <div class="alert alert-info english-homework" role="alert" style="background-color: false;">
                <p id="` + weekData.week + `OnlineLesson" style="margin: 30px;">
                    <strong>Set</strong> <span>` + weekData.week +`</span>&nbsp;&nbsp;<i class="bi-mortarboard-fill h5 text-primary"></i>
                </p>
                <div class="progress" style="margin: 30px;">
                    <div id="` + weekData.id + `Bar" class="`+ getProgressBarClass(weekData.percentage) +`" role="progressbar" style="width: ` + weekData.percentage + `%;" aria-valuemin="0" aria-valuemax="100">
                        <span id="` + weekData.id + `Percentage" class="ml-auto pl-2">` + weekData.percentage +`%</span>
                    </div>
                </div>
            </div>
        </div>
    `;
    return card;
}

function createNextCard(week) {
    const card = document.createElement('div');
    card.className = 'col-md-6';
    card.innerHTML = `
        <div class="card-body mx-auto" style="max-width: 75%;">
            <div class="alert alert-secondary english-homework" role="alert" style="background-color: lightgrey;">
                <p style="margin: 30px;">
                    <strong>Set</strong> <span>` + week + `</span>
                    &nbsp;&nbsp;<i class="bi bi-lock-fill h5 text-secondary"></i>
                </p>
                <div class="progress" style="margin: 30px;">
                    <div class="progress-bar" role="progressbar" style="width: 0%;" aria-valuemin="0" aria-valuemax="100">
                        <span class="ml-auto text-secondary pl-2">0%</span>
                    </div>
                </div>
            </div>
        </div>
    `;
    return card;
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Color Progress Bar
////////////////////////////////////////////////////////////////////////////////////////////////////////
function getProgressBarClass(percentage) {
    if (percentage < 30) {
        return 'progress-bar bg-danger'; // Red color for less than 30%
    } else if (percentage >= 30 && percentage <= 70) {
        return 'progress-bar bg-warning'; // Yellow color for 30% - 70%
    } else {
        return 'progress-bar bg-success'; // Green color for more than 70%
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Display Card
////////////////////////////////////////////////////////////////////////////////////////////////////////
function displayCards() {
    const container = document.getElementById('cardsContainer');
    weeksData.forEach(weekData => {
        const card = createCard(weekData);
        container.appendChild(card);
    });
    // add next card
    const nextCard = createNextCard(academicWeek+1);
    container.appendChild(nextCard);
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
// 			Update Progress
////////////////////////////////////////////////////////////////////////////////////////////////////////
function updateProgressOnServer(homeworkId, percentage){
    // Remove the '%' symbol and convert to integer
    var percentageValue = parseInt(percentage.replace('%', ''), 10);
    // console.log('Updating progress:', homeworkId, percentageValue, studentId);
   
    $.ajax({
        url: '${pageContext.request.contextPath}/connected/updateHomeworkProgress',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            homeworkId: homeworkId,
            studentId: studentId,
            percentage: percentageValue
        }),
        success: function(response) {
            console.log('Progress updated successfully:', response);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error updating progress:', errorThrown);
        }
    });
}

</script>

<input type="hidden" id="academicYear" name="academicYear" />
<div class="col-md-12 pt-3">
    <div class="card-body text-center">
        <h2 style="color: #6c757d; font-weight: bold; text-transform: uppercase; text-shadow: 2px 2px 4px rgba(247, 247, 161, 1);">Mathematics Homework</h2>
    </div>
</div>
<div class="container mt-3" style="background: linear-gradient(to right, #f9f9d5 0%, #f7f7a1 100%); border-radius: 15px;">   
    <div id="cardsContainer" class="row mt-5"></div>
</div>

<!-- Pop up Video modal -->
<div class="modal fade" id="homeworkModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true"  data-backdrop="static" data-keyboard="false">
    <div class="modal-dialog modal-extra-large" role="document">
        <div class="modal-content" style="height: 90vh;">
            <div class="modal-header bg-primary text-white text-center">
                <h5 class="modal-title w-100" id="exampleModalLabel">Mathematics Homework Details - Set <span id="dialogSet" name="dialogSet" class="text-warning"></span></h5>
                <button type="button" class="close position-absolute" style="right: 1rem;" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body bg-light">
                <div class="row">
                    <div class="col-md-6 d-flex justify-content-center bg-white p-3 border">
                        <video id="videoPlayer" controls controlsList="nodownload" style="width: 100%; height: auto;">
                            <source src="" type="video/mp4">
                        </video>
                    </div>
                    <div class="col-md-6 bg-white p-3 border">
                        <object id="pdfViewer" data="" type="application/pdf" style="width: 100%; height: 80vh;">
                            <p>It appears you don't have a PDF plugin for this browser. No biggie... you can <a href="your_pdf_url">click here to download the PDF file.</a></p>
                        </object>
                    </div>
                </div>
            </div>
            <div class="modal-footer bg-dark text-white">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
