
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

function alterarDisplayDivNovaAtividade(){

	var divNovaAtividade = document.getElementById("divNovaAtividade");
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