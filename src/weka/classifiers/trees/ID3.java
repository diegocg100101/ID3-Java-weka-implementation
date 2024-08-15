/**
 * Implementación de árbol de decisiones ID3
 */
package weka.classifiers.trees;

import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Attribute;

/**
 * @author Diego
 */
public class ID3 extends AbstractClassifier {

	/**
	 * Variable global del nodo raíz del árbol para evaluar
	 */
	Nodo raiz;

	private static final long serialVersionUID = 1789045553777829235L;


	/**
	 * @param datos	Toma un conjunto de instancias y construye el árbol
	 */
	@Override
	public void buildClassifier(Instances datos) throws Exception {
		// Llama a la función "arbol()" para construir el árbol
		raiz = arbol(datos);
		System.out.println(raiz);
	}

	/**
	 * Método que construye el árbol
	 * @param datos Instancias
	 * @return Nodo raiz del árbol
	 */
	public Nodo arbol(Instances datos){

		// Recupera el mejor atributo
		Attribute mejorAtributo = encontrarMejor(datos);

		// Valida que el nodo recibido no sea nulo
		if(mejorAtributo != null){
			Nodo mejor = new Nodo(mejorAtributo);

			// Inicializa índice
			int index = 0;

			// Encuentra el índice del mejor atributo
			for(int i = 0; i < datos.numAttributes(); i ++){
				if(datos.attribute(i).equals(mejorAtributo)){
					index = i;
					break;
				}
			}

			// Itera los valores del mejor atributo
			for(int i = 0; i < mejorAtributo.numValues(); i++){
				
				// Inicializar la variable que almacenará las instancias
				Instances nuevosDatos = new Instances(datos);
				nuevosDatos.delete();
		
				// Filtro únicamente las instancias con el valor de la rama
				for(Instance dato : datos){
					if(dato.value(mejorAtributo) == mejorAtributo.indexOfValue(mejorAtributo.value(i))){
						nuevosDatos.add(dato);
					}
				}

				// Cuenta los valores para las clases "sí" y "no"
				int contadorYes = 0, contadorNo = 0;
				for(Instance dato : nuevosDatos){
					if(dato.classValue() == 1){
						contadorYes ++;
					} else {
						contadorNo ++;
					}
				}

				// Verifica que los contadores sean o no iguales al tamaño de las instancias, de ser así, se agrega una hoja en lugar de un sub-árbol
				if(contadorYes == nuevosDatos.size()){
					mejor.AgregaHijo( new Nodo(datos.classAttribute().value(1)), mejorAtributo.value(i));
				} else if (contadorNo == nuevosDatos.size()) {
					mejor.AgregaHijo( new Nodo(datos.classAttribute().value(0)), mejorAtributo.value(i));
				} else {
					nuevosDatos.deleteAttributeAt(index);
					Nodo subArbol = arbol(nuevosDatos);
					mejor.AgregaHijo(subArbol, mejorAtributo.value(i));
				}
			}

			// Regresa el nodo incial con los hijos agregados
			return mejor;
		}
		// Regresa un nodo sin importancia si el mejor atributo resulta null
		return new Nodo("");
	}

	/**
	 * Método que encuentra el atributo con mayor ganancia en un conjunto de datos
	 * @param datos Instancias
	 * @return
	 */
	private Attribute encontrarMejor(Instances datos) {
		// Inicializa mayor ganancia con un valor infinito negativo para ser reemplazado
		double mayorGanancia = Double.NEGATIVE_INFINITY;

		// Inicializa número total de atributos
		int totalAtributos = datos.numAttributes();

		// Establece la clase: play {no : 0, yes : 1}
		datos.setClassIndex(datos.numAttributes() - 1);

		// Inicializa arreglo de "double" con el tamaño de los atributos
		double[] contadorClase = new double[datos.attribute(totalAtributos - 1).numValues()]; 

		// Cálculo de la entropía de la clase
		for(Instance dato : datos){
			contadorClase[(int) dato.classValue()] ++;
		}

		double entropiaClase = calcularEntropia(contadorClase);

		// Inicializa la ganancia
		double ganancia = 0.0;

		// Inicializa el mejor atributo a seleccionar
		Attribute mejorAtributo = null;

		// Cálculo de ganancias y eleccción del atributo con mayor ganancia
		for(int i = 0; i < totalAtributos - 1; i++){

			// Calcula la ganancia 
			ganancia = calcularGanancia(datos, datos.attribute(i), entropiaClase);

			// Busca el nodo con mayor ganancia
			if(ganancia > mayorGanancia){
				mejorAtributo = datos.attribute(i);
				mayorGanancia = ganancia;
			}
		}
		return mejorAtributo;
	}

	/**
	 * Toma una instancia de ejemplo y la clasifica con base al árbol
	 * @param ejemplo Instancia de ejemplo
	 */
	@Override
	public double classifyInstance(Instance ejemplo) throws Exception {
		String valor = raiz.Evalua(ejemplo);
		if(valor == "no"){
			return 0.0;
		} 
		return 1.0;
	}

	/**
	 * Calcula la entropía de los valores de las clases
	 * @param valores Arreglo de dobles que contiene el número de valores de las clases
	 */
	public double calcularEntropia(double[] valores) {
		// E = -p(+) log_2 (p(+)) - p(-) log_2 (p(-))
		double entropia = 0.0;
		double p = 0.0;
		double total = 0.0;
		
		// Obtiene el número total de casos
		for(double valor : valores){
			total += valor;
		}

		// Calcula cada término de la entropía en el arreglo
		for(double valor: valores){
			// p (probabilidad) = (cantidad positivos o negativos)/total
			p = (double) valor / total;

			// Evitar logaritmos negativos
			if(p == 0){
				return entropia = 0.0;
			}
			entropia -= p * (Math.log(p) / Math.log(2));
		}

		return entropia;
	}
	

	/**
	 * Calcula la entropía de las ramas de un atributo y la ganancia
	 * @param datos			Instancias
	 * @param atributo		Atributo en cuestión a evaluar sus ramas
	 * @param entropiaTotal	Entropía de todas las instancias
	 * @return 				Ganancia
	 */
	public double calcularGanancia(Instances datos, Attribute atributo, double entropiaTotal){
		double[] valores;
		double entropias = 0.0;

		// Itera los valores del atributo
		for(int i = 0; i < atributo.numValues(); i ++){

			// En cada iteración reinicia el arreglo de valores de clase
			valores = new double[2];

			// Itera las instancias
			for(Instance dato : datos){

				// Determina si el valor del atributo en esa instancia es igual al valor actual
				if(dato.stringValue(atributo).equals(atributo.value(i))){

					// Aumenta contador
					valores[(int) dato.classValue()] ++;
				}
			}

			// Aumenta el término negativo de la ecuación
			// E_rama = E_t * p(total) 
			entropias -= calcularEntropia(valores) * (valores[0] + valores[1]) / datos.numInstances();
		}

		// G(S, Atributo) = E_S - suma(E_ramas)
		return entropiaTotal + entropias;
	}

	/**
	 * Imprime el árbol en weka
	 */
	@Override
	public String toString() {
		return raiz.toString();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Para pruebas
		ID3 id3 = new ID3();

		// Lee datos de tenis
		DataSource source = new DataSource("weather.nominal.arff");
		Instances datos = source.getDataSet();

		Instance ejemplo = datos.get(2);
		id3.buildClassifier(datos);
		double resultado = id3.classifyInstance(ejemplo);
		System.out.println(resultado);
	}

}
