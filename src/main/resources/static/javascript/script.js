
function isSenhaIguais(){
	
	var senha = document.getElementById("senha").value;
	var senhaRepetida = document.getElementById("senhaRepetida").value;
	
	
	if(senha != senhaRepetida){
		document.getElementById("mensagemErroValidacaoJS").style.display = "block";
		var senha = document.getElementById("senha").value = "";
		var senhaRepetida = document.getElementById("senhaRepetida").value = "";
	} else if(senha == senhaRepetida){
		document.getElementById("mensagemErroValidacaoJS").style.display = "none";
	}
}

function alterarDisplayDiv(idDiv){

	var divNovaAtividade = document.getElementById(idDiv);
	var msgSucesso = document.getElementById("mensagemSucesso");
	
	if(divNovaAtividade.style.display == "none"){
		divNovaAtividade.style.display = "block";
		msgSucesso.style.display="none";
		
	} else if(divNovaAtividade.style.display == "block"){
		divNovaAtividade.style.display = "none";
	}
}


function confirmarEncerramento(){
	var select = document.getElementById("area").value;
	var statusAtividade = document.getElementById("statusAtividade").value;
	
	if(select == 'FINALIZADA' && statusAtividade != 'FINALIZADA'){
		var result = confirm('Tem certeza que deseja encerrar atividade?');
		if(result == false){
			document.getElementById("area").value = statusAtividade;
		}
		return result;
	}
	return true;
}

function verificarAtualizacaoDeArquivoDeAtividade(){
	var fileUpload = document.getElementById("atividade");
	if(fileUpload.files.length > 0){
		var result = confirm('Isso irá substituir, se caso existir, o arquivo adicionado anteriormente a esta atividade! Você tem certeza que deseja fazer isso?');
		if(result == false){
			var fileUpload = document.getElementById("atividade").value = "";
		}
	}
}


function confirmarEEncerrarCurso() {
	var decisao = confirm('Isso irá alterar o status do curso para concluído e essa ação não pode ser desfeita, você tem certeza que deseja fazer isso?');
	if(decisao) {
		var idCurso = document.getElementById("idCurso").value;
		var url = window.location.href;
		url = url.split('/curso');
		url = url[0];
		url = url + '/curso/encerrar?curso=' + idCurso;
		window.location.href = url;
	}
}

function confirmarEDesmatricularAluno(end, idAluno, idCurso, isPageAluno) {
	var decisao = confirm('Tem certeza que deseja desmatricular este aluno?');
	if(decisao) {
		var url = window.location.href;
		if(isPageAluno === 'true') {
			url = url.split('/aluno');
		} else {
			url = url.split('/curso');
		}
		url = url[0];
		url = url + end + '/desmatricular?aluno=' + idAluno + '&curso=' + idCurso;
		window.location.href = url;
	}
}


function confirmarExclusaoProfessor(){
	var decisao = confirm('Tem certeza que deseja excluir este professor?');
	if(decisao){
		var idProfessor = document.getElementById('idProfessor').value;
		var url = window.location.href;
		url = url.split('/professor');
		url = url[0];
		url = url + '/professor/excluirProfessor?id=' + idProfessor;
		window.location.href = url;
	}
}