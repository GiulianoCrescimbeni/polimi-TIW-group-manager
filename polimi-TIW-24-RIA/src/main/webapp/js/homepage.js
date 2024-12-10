$(document).ready(function() {
    let invitationErrorCounter = 0;
    let minParticipants = 0;
    let maxParticipants = 0;

    function initializeDragAndDrop() {
        $('.member').draggable({
            helper: "clone",
            appendTo: "body",
            zIndex: 10000
        });

        $('#trash-bin').droppable({
            accept: ".member",
            drop: function(event, ui) {
                var memberId = ui.helper.data('member-id');
                console.log("Dropping member ID:", memberId); // Debug log
                $.ajax({
                    url: 'removemember',
                    type: 'POST',
                    data: {
                        memberId: memberId,
                        groupId: $('#group-details').data('group-id')
                    },
                    success: function(response) {
                        showNotificationModal('Member successfully removed');
                        updateGroupDetails($('#group-details').data('group-id'));
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        showNotificationModal("Error: " + jqXHR.responseText);
                    }
                });
            }
        });
    }

    function showNotificationModal(message) {
        $('#notification-modal .modal-body').text(message);
        $('#notification-modal').show();
    }

    function updateGroupDetails(groupId) {
        $.ajax({
            url: 'groupdetails',
            type: 'POST',
            data: { id: groupId },
            dataType: 'json',
            success: function(response) {
                console.log("Response:", response); // Debug log
                $('#group-details').html(response.html);
                $('#group-details').data('group-id', groupId); // Salva l'ID del gruppo

                // Verifica se l'utente è l'amministratore del gruppo
                var isAdmin = response.isAdmin;
                if (isAdmin) {
                    $('#trash-bin').show();
                    initializeDragAndDrop();
                } else {
                    $('#trash-bin').hide();
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                showNotificationModal("Error: " + jqXHR.responseText);
            }
        });
    }

    $('#create-group-form').submit(function(event) {
        event.preventDefault(); 
        var formData = $(this).serialize(); 

        var params = new URLSearchParams(formData);
        maxParticipants = params.get('maximumParticipants');
        minParticipants = params.get('minimumParticipants');

        $.ajax({
            url: 'creategroup',
            type: 'POST',
            data: formData,
            success: function(response) {
                $('#invitation-form').html(response.html);
                $('#invitation-form-modal').show(); 
            },
            error: function(jqXHR, textStatus, errorThrown) {
                showNotificationModal("Error: " + jqXHR.responseText);
            }
        });
    });

    $('#invitation-form-id').submit(function(event) {
        event.preventDefault();
        // Seleziona tutte le checkbox selezionate nel form
        const checkboxes = document.querySelectorAll('#invitation-form-id input[type="checkbox"]:checked');
        const selectedUsers = [];

        // Ottieni i valori delle checkbox selezionate
        checkboxes.forEach((checkbox) => {
            selectedUsers.push(checkbox.value);
        });

        if (selectedUsers.length > maxParticipants) {
            if (invitationErrorCounter == 2) {
                invitationErrorCounter = 0;
                minParticipants = 0;
                maxParticipants = 0;
                showNotificationModal("Too many failed attempts to create a group. Retry");
                $.ajax({
                    url: 'clean',
                    type: 'GET',
                    success: function(response) {
                        $('#invitation-form-modal').hide();
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        showNotificationModal("Error: " + jqXHR.responseText);
                    }
                });
                return;
            }

            showNotificationModal("Too many users selected, remove " + (selectedUsers.length - maxParticipants) + " user/s");
            invitationErrorCounter++;
            return;
        } else if (selectedUsers.length < minParticipants) {
            if (invitationErrorCounter == 2) {
                invitationErrorCounter = 0;
                minParticipants = 0;
                maxParticipants = 0;
                showNotificationModal("Too many failed attempts to create a group. Retry");
                $.ajax({
                    url: 'clean',
                    type: 'GET',
                    success: function(response) {
                        $('#invitation-form-modal').hide();
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        showNotificationModal("Error: " + jqXHR.responseText);
                    }
                });
                return;
            }

            showNotificationModal("Few selected users, add " + (minParticipants - selectedUsers.length) + " user/s");
            invitationErrorCounter++;
            return;
        }

        $.ajax({
            url: 'submitgroup',
            type: 'POST',
            data: {participants: selectedUsers},
            traditional: true,
            success: function(response) {
                showNotificationModal("Group successfully created");
                $.ajax({
                    url: 'updategroups',
                    type: 'GET',
                    success: function(response) {
                        $('#invitation-form-modal').hide();
                        $('#groups-display').html(response.html);
                        attachGroupLinkHandlers(); // Riassegna gli handler agli elementi dinamici
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        showNotificationModal("Error: " + jqXHR.responseText);
                    }
                });
            },
            error: function(jqXHR, textStatus, errorThrown) {
                showNotificationModal("Error: " + jqXHR.responseText);
                $.ajax({
                    url: 'clean',
                    type: 'GET',
                    success: function(response) {
                        $('#invitation-form-modal').hide();
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        showNotificationModal("Error: " + jqXHR.responseText);
                    }
                });
            }
        });
    });

    function attachGroupLinkHandlers() {
        $(document).on('click', '.group-link', function() {
            var groupId = $(this).data('group-id');
            console.log("Group ID:", groupId); // Debug log
            updateGroupDetails(groupId);
            $('#group-details-modal').show();
        });
    }

    // Inizialmente assegna gli handler agli elementi esistenti
    attachGroupLinkHandlers();

    // Chiudi le finestre modali quando l'utente clicca sulla "X"
    $('.close').click(function() {
        $(this).closest('.modal').hide();
    });

    // Chiudi la finestra modale quando l'utente clicca fuori dal contenuto della finestra
    $(window).click(function(event) {
        if ($(event.target).is('#group-details-modal')) {
            $('#group-details-modal').hide();
            $('#trash-bin').hide();
        }
        if ($(event.target).is('#error-modal') || $(event.target).is('#notification-modal') || $(event.target).is('#additional-form-modal')) {
            $(event.target).hide();
        }
    });

    $('#invitation-close').click(function(event) {
        $.ajax({
            url: 'clean',
            type: 'GET',
            success: function(response) {
                $('#invitation-form-modal').hide();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                showNotificationModal('Error: ' + textStatus);
            }
        });
    });
});
