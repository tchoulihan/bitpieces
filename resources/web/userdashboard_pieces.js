
$(document).ready(function(){


    sessionId = getCookie("authenticated_session_id");
    console.log(sessionId);

    fillUserHighChartStandardTemplate('get_pieces_owned_value_accum', "#pieces_owned_value", 'Value ($)', '$');
    fillUserHighChartStandardTemplate('get_pieces_owned_accum', '#pieces_owned', '# of Pieces owned', '');
    fillUserHighChartPieChartTemplate('get_pieces_owned_value_current', '#pieces_owned_value_current');
    fillUserHighChartStandardTemplate('get_rewards_earned', '#rewards_earned', 'Reward ($)', '$');
});


