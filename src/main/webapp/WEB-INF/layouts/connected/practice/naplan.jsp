<script>



const PRACTICE_GROUP = 5; // 5 is NAPLAN


const PRACTICE_TYPE = 6; // 6 is NAPLAN Language Conventions
const MOVIE = 0;
const PDF = 1;
const DONE= 'DONE';
// console.log(studentId);
$(function() {
    $.ajax({
        url : '${pageContext.request.contextPath}/connected/summaryPractice/' + PRACTICE_GROUP + '/' + numericGrade,
        method: "GET",
        success: function(data) {
            $.each(data, function(index, basket) {
                console.log(basket);
                var title = basket.title;
                var id = basket.id;
                var week = basket.week;
                var icon = '<i class="bi bi-send h5 text-primary" title="unsubmitted yet"></i>';
                var cardBody = '<div class="card-body mx-auto" style="cursor: pointer; max-width: 75%; min-width: 235px;" onclick="displayMaterial(' + id +  ', \'' +  title + '\');">'
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
                + '<strong><span id="topicTitle">Set ' + week + ' - '+ title + '</span></strong>&nbsp;&nbsp;' + icon
                + '</p></div></div></div>';
                $('#topicContainer').append(topicDiv);    
            });
            document.getElementById("practiceModalLabel").innerHTML = 'NAPLAN Language Conventions Practice - Set <span id="dialogSet" name="dialogSet" class="text-warning"></span>';            
            document.getElementById("practiceAnswerModalLabel").innerHTML = 'NAPLAN Language Conventions Practice - Set <span id="dialogAnswerSet" name="dialogAnswerSet" class="text-warning"></span>';            
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });
});

</script>
    
<div class="col-md-12 pt-3">
    <div class="card-body text-center">
        <h2 style="color: #6c757d; font-weight: bold; text-transform: uppercase; text-shadow: 2px 2px 4px rgba(235, 177, 249, 1);">Naplan Practice</h2>
    </div>
</div>

<div class="container mt-3" style="background: linear-gradient(to right, #f7eef9 0%, #F8e2fd 100%); border-radius: 15px;">
    <div id="topicContainer" class="row mt-5 mb-5 justify-content-center"></div>
</div>

<!-- Include test.jsp -->
<jsp:include page="practice.jsp" />
    
    