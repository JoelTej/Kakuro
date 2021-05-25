package main;

import domainLayer.CtrlDomini;
import presentationLayer.CtrlPresentation;

//clase main de la aplicacion
public class Main {
	public static void main (String [] args){
		//iniciamos los precandidatos del solver
		CtrlDomini.initializeSolver();
		//iniciamos la UI
		CtrlPresentation.initializePresentation();
	}
}