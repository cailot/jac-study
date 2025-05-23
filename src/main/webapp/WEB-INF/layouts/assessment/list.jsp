<style>
    .assessment-container {
        background: white;
        border-radius: 10px;
        padding: 2rem;
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        max-width: 500px;
        width: 100%;
        text-align: center;
    }
    .assessment-container h1 {
        font-size: 2rem;
        margin-bottom: 1rem;
        font-weight: bold;
    }
    .assessment-container .btn {
        font-size: 1.25rem;
        padding: 0.5rem;
        margin: 0.5rem 0;
        width: 100%;
    }
    .btn.disabled {
        cursor: not-allowed;
    }
</style>

<div class="assessment-container">
    <h1>Online Assessment</h1>
    <button class="btn btn-primary" id="mathTest" onclick="showWarning(1)">MATHS</button>
    <button class="btn btn-primary" id="englishTest" onclick="showWarning(2)">ENGLISH</button>
    <button class="btn btn-primary" id="gaTest" onclick="showWarning(3)">GA</button>
    <button class="btn btn-secondary disabled mt-3" id="selectionCount"><span id="selectCountDisplay">0</span> / <span id="totalCountDisplay">0</span></button>
</div>

<!--Test Warning Modal -->
<div class="modal fade" id="testWarningModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" role="document">
        <div class="modal-content" style="border: 2px solid #ffc107; border-radius: 10px;">
            <div class="modal-header bg-warning" style="display: block;">
                <p style="text-align: center; margin-bottom: 0;"><span style="font-size:18px"><strong>Assessment Instruction for James An College</strong></span></p>
            </div>
            <div class="modal-body" style="background-color: #f8f9fa; border-radius: 5px; padding: 20px;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <img src="${pageContext.request.contextPath}/image/test.png" style="width: 150px; height: 150px; border-radius: 5%;">
                </div>
                <!-- Add your warning message or content here -->
                <ol style="line-height: 1.6;">
                    <li><span class="text-primary"><strong>Assessment Duration</strong></span>
                        Ensure completion within the 20-minute time limit provided for the assessment.
                    </li>
                    <li><span class="text-primary"><strong>Single Attempt</strong></span>
                        Each student has a single opportunity to attempt the assessment, and once initiated, retakes are not permitted.
                    </li>
                    <li><span class="text-primary"><strong>Submission</strong></span>
                        Upon finishing the assessment, submit your answers using the "Submit" button; changes cannot be made thereafter.
                    </li>
                    <li><span class="text-primary"><strong>Feedback</strong></span>
                        Result will be sent to selected branch directly. View both your answers and the correct ones for each question immediately after submission, facilitating review and learning strategy.
                    </li>
                </ol>
                <p><strong>We hope you do your best with your assessment!</strong></p>      
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="agreeTestWarning">I understand</button>
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>
<script>
var selectedCount = 0;
// Extract 'id' and 'grade' from the current URL
let currentId = getQueryParam('id');
let currentGrade = getQueryParam('grade');

$(document).ready(function () {
    $.ajax({
        url: '${pageContext.request.contextPath}/assessment/listSubject/' + currentGrade,
        type: 'GET',
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            if (data > 2) {
                // display GA
                document.getElementById('gaTest').style.display = 'block';
                updateTotalCountDisplay(3);
            } else {
                // hide GA
                document.getElementById('gaTest').style.display = 'none';
                updateTotalCountDisplay(2);
            }
            updateSelectionCount(); // Call updateSelectionCount after updating totalCountDisplay
        },
        error: function (xhr, status, error) {
            console.log('Error Details: ' + error);
        }
    });

    if ((getQueryParam('math') != null) || (getQueryParam('english') != null) || (getQueryParam('ga') != null)) {
        modifyButtonBasedOnUrl();
    }
});

// update totalCount
function updateTotalCountDisplay(count) {
    console.log('Updating total count display to:', count); // Debugging log
    document.getElementById('totalCountDisplay').textContent = count;
}

// update selectionCount
function updateSelectionCount() {
    const submitCount = document.getElementById('selectCountDisplay');
    submitCount.textContent = selectedCount;
    const totalCount = parseInt(document.getElementById('totalCountDisplay').textContent, 10);

    console.log(selectedCount + '---> ' + totalCount);
    if (selectedCount === totalCount) {
        // enable button click
        const selectionCountButton = document.getElementById('selectionCount');
        selectionCountButton.classList.remove('btn-secondary');
        selectionCountButton.className = 'btn btn-primary';
        selectionCountButton.disabled = false;
        selectionCountButton.addEventListener('click', sendEmail);
        // change label
        selectionCountButton.textContent = 'SUBMIT';
    }
}

function getQueryParam(param) {
    var urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

window.showWarning = function(id) {
    // Show the warning modal
    $('#testWarningModal').modal('show');
    // Attach the click event handler to the "I agree" button
    $('#agreeTestWarning').one('click', function() {
        // Redirect based on the test id
        if (id == 1) {
            window.location.href = '${pageContext.request.contextPath}/assessment/math?id=' + currentId + '&grade=' + currentGrade;
        } else if (id == 2) {
            window.location.href = '${pageContext.request.contextPath}/assessment/english?id=' + currentId + '&grade=' + currentGrade;
        } else if (id == 3) {
            window.location.href = '${pageContext.request.contextPath}/assessment/ga?id=' + currentId + '&grade=' + currentGrade;
        }
        $('#testWarningModal').modal('hide');
    });
}

// Function to disable and change the 'mathTest' button's color if 'math=true' in the URL
function modifyButtonBasedOnUrl() {
    var mathParam = getQueryParam('math');
    if (mathParam === 'true') {
        var button = document.getElementById('mathTest');
        button.className = 'btn btn-secondary'; // Change color to grey
        button.disabled = true; // Disable the button
        button.onclick = null; // Remove onclick event to disable it
        selectedCount++;
    }

    var engParam = getQueryParam('english');
    if (engParam === 'true') {
        var button = document.getElementById('englishTest');
        button.className = 'btn btn-secondary'; // Change color to grey
        button.disabled = true; // Disable the button
        button.onclick = null; // Remove onclick event to disable it
        selectedCount++;
    }

    var gaParam = getQueryParam('ga');
    if (gaParam === 'true') {
        // Select the 'mathTest' button by its ID
        var button = document.getElementById('gaTest');
        button.className = 'btn btn-secondary'; // Change color to grey
        button.disabled = true; // Disable the button
        button.onclick = null; // Remove onclick event to disable it
        selectedCount++;
    }
    updateSelectionCount();
}

// request sending results via email
function sendEmail() {
    // Show loading spinner
    $('#loading-message').text('Sending results to email');
    $('#loading-spinner').modal('show');
    // Send AJAX to server
    $.ajax({
        url: '${pageContext.request.contextPath}/assessment/sendResult/' + currentId,
        type: 'GET',
        dataType: 'json',
        success : function(response) {
            $('#loading-spinner').modal('hide');    
            if (response.status === 'success') {
                $('#success-alert .modal-body').html(response.message);
                $('#success-alert').modal('show');
                $('#success-alert').on('hidden.bs.modal', function (e) {
                    document.getElementById('mathTest').disabled = true;
                    document.getElementById('englishTest').disabled = true;
                    document.getElementById('gaTest').disabled = true;
                    document.getElementById('selectionCount').disabled = true;
                });
            } else {
                $('#validation-alert .modal-body').html(response.message || 'Failed to send email.');
                $('#validation-alert').modal('show');
            }
        },
        error : function(xhr, status, error) {
            $('#loading-spinner').modal('hide');
            console.log('Error:', error);
            console.log('Status:', status);
            console.log('Response:', xhr.responseText);
            
            let errorMessage = 'Failed to send email.';
            try {
                const response = JSON.parse(xhr.responseText);
                if (response.message) {
                    errorMessage = response.message;
                }
            } catch(e) {
                console.log('Error parsing response:', e);
            }
            
            $('#validation-alert .modal-body').html(errorMessage);
            $('#validation-alert').modal('show');
        }
    });
}
</script>

<!-- Success Alert -->
<div id="success-alert" class="modal fade">
	<div class="modal-dialog">
		<div class="alert alert-block alert-success alert-dialog-display jae-border-success">
			<i class="fa fa-check-circle fa-2x"></i>&nbsp;&nbsp;<div class="modal-body"></div>
			<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
		</div>
	</div>
</div>

<!-- Validation Alert -->
<div id="validation-alert" class="modal fade">
    <div class="modal-dialog">
        <div class="alert alert-block alert-danger alert-dialog-display jae-border-danger">
            <i class="bi bi-exclamation-circle-fill h5 mt-2"></i>&nbsp;&nbsp;<div class="modal-body"></div>
            <a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>
        </div>
    </div>
</div>