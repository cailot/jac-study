<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<script>

const TEST_TYPE = 11; // 11 is Humanities (ACER)
const DONE= 'DONE';

$(function() {
    $.ajax({
        url : '${pageContext.request.contextPath}/connected/summaryTest/' + studentId + '/' + TEST_TYPE + '/' + numericGrade,
        method: "GET",
        success: function(data) {
            $.each(data, function(index, basket) {
                var title = basket.name;
                var id = basket.value;
                var icon = '<i class="bi bi-send h5 text-primary" title="unsubmitted yet"></i>';
                var cardBody = '<div class="card-body mx-auto" style="cursor: pointer; max-width: 75%; min-width: 235px;" onclick="showWarning(' + id +  ', \'' +  title + '\');">'
                if (title.endsWith('DONE')) {
                    // title ends with 'DONE'
                    title = title.slice(0, -4);
                    icon = '<i class="bi bi-send-fill h5 text-primary" title="submitted"></i>';
                    cardBody = '<div class="card-body mx-auto" style="cursor: pointer; max-width: 75%; min-width: 235px;" onclick="displayAnswer(' + id +  ', \'' +  title + '\');">'
                }
                var columnClass = data.length === 2 ? 'mr-5' : ''; // padding in case of 2 cards
                var topicDiv = '<div class="col-md-4 ' + columnClass + '">'
                + cardBody
                + '<div class="alert alert-info topic-card" role="alert"><p id="onlineLesson" style="margin: 30px;">'
                + '<strong><span id="topicTitle">Volume ' + title + '</span></strong>&nbsp;&nbsp;' + icon
                + '</p></div></div></div>';
                $('#topicContainer').append(topicDiv);    
            });
            // always 3 update in case of video & pdf resources provided
            document.getElementById("testModalLabel").innerHTML = 'Humanities (ACER) Test - Volume <span id="dialogSet" name="dialogSet" class="text-warning"></span>';
            document.getElementById("testAnswerModalLabel").innerHTML = 'Humanities (ACER) Test - Volume <span id="dialogAnswerSet" name="dialogAnswerSet" class="text-warning"></span>';
            document.getElementById("testAnswerPdfModalLabel").innerHTML = 'Humanities (ACER) Test - Volume <span id="dialogAnswerPdfSet" name="dialogAnswerPdfSet" class="text-warning"></span>';            
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });
});

window.showWarning = function(id, title) {
    // Show the warning modal
    $('#testWarningModal').modal('show');
    // Attach the click event handler to the "I agree" button
    $('#agreeTestWarning').one('click', function() {
        displayMaterial(id, title);
        $('#testWarningModal').modal('hide');
    });
}
</script>

<div class="col-md-12 pt-3">
    <div class="card-body text-center">
        <h2 style="color: #6c757d; font-weight: bold; text-transform: uppercase; text-shadow: 2px 2px 4px rgba(168, 179, 247, 1);">Humanities (ACER)</h2>
    </div>
</div>

<div class="container mt-3" style="background: linear-gradient(to right, #f1f3ff 0%, #b1b9f9 100%); border-radius: 15px;">
    <div id="topicContainer" class="row mt-5 mb-5 justify-content-center"></div>
</div>

<!-- Include test.jsp -->
<jsp:include page="test.jsp" />