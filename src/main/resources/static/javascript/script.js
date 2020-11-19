
function isSenhaIguais(){
	
	var senha = document.getElementById("senha").value;
	var senhaRepetida = document.getElementById("senhaRepetida").value;
	
	
	if(senha != senhaRepetida){
		document.getElementById("mensagemErroValidacaoJS").style.display = "block";
		var senha = document.getElementById("senha").value = "";
		var senhaRepetida = document.getElementById("senhaRepetida").value = "";
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