<script>
const PRACTICE_GROUP = 2; // 2 is REVISION
const DONE= 'DONE';
$(function() {
    $.ajax({
        url : '${pageContext.request.contextPath}/connected/summaryPractice/' + PRACTICE_GROUP + '/' + studentId + '/' + numericGrade,
        method: "GET",
        success: function(data) {
            $.each(data, function(index, basket) {
                // console.log(basket);
                var title = basket.title;
                var id = basket.id;
                var week = basket.week;
                var setName = week;
                switch (week) {
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
                var icon = '<i class="bi bi-send h5 text-primary" data-toggle="tooltip" title="Not Submitted Yet"></i>';
                var cardBody = '<div class="card-body mx-auto text-center" style="cursor: pointer; max-width: 75%; min-width: 235px;" onclick="displayMaterial(' + id +  ');">'
                if (title.endsWith('DONE')) {
                    // title ends with 'DONE'
                    title = title.slice(0, -4);
                    icon = '<i class="bi bi-send-fill h5 text-primary" data-toggle="tooltip" title="submitted"></i>';
                    cardBody = '<div class="card-body mx-auto text-center" style="cursor: pointer; max-width: 75%; min-width: 235px;" onclick="displayAnswer(' + id + ', \'' + title + '\', \'' + setName + '\');">'
                }
                var columnClass = data.length === 2 ? 'mr-5' : ''; // padding in case of 2 cards
                var topicDiv = '<div class="col-md-4 ' + columnClass + '">'
                + cardBody
                + '<div class="alert alert-info topic-card" role="alert"><p id="onlineLesson" style="margin-top: 30px; margin-bottom: 30px;">'
                + '<strong><span id="topicTitle" class="badge badge-primary custom-badge">' + title + '</span></strong><br>Set <strong><i>' + setName +'</i></strong>&nbsp;&nbsp;' + icon
                + '</p></div></div></div>';
                $('#topicContainer').append(topicDiv);    
            });
             // Reinitialize tooltips after content is added
             $('[data-toggle="tooltip"]').tooltip();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log('Error : ' + errorThrown);
        }
    });
});
</script>
  
<div class="col-md-12 pt-3">
    <div class="card-body text-center">
        <h2 style="color: #6c757d; font-weight: bold; text-transform: uppercase; text-shadow: 2px 2px 4px rgba(235, 177, 249, 1);">Revision Practice</h2>
    </div>
</div>

<div class="container mt-3" style="background: linear-gradient(to right, #f7eef9 0%, #F8e2fd 100%); border-radius: 15px;">
    <div id="topicContainer" class="row mt-5 mb-5 justify-content-center"></div>
</div>

<!-- Include test.jsp -->
<jsp:include page="practice.jsp" />
