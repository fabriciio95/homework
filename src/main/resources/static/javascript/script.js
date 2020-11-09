
function isSenhaIguais(){
	
	var senha = document.getElementById("senha").value;
	var senhaRepetida = document.getElementById("senhaRepetida").value;
	
	
	if(senha != senhaRepetida){
		document.getElementById("mensagemErroValidacaoJS").style.display = "block";
		var senha = document.getElementById("senha").value = "";
		var senhaRepetida = document.getElementById("senhaRepetida").value = "";
	}

}