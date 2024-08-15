package weka.classifiers.trees;
/**
 * Un nodo para un árbol de desición
 */

import java.util.ArrayList;
import weka.core.Instance;
import weka.core.Attribute;


public class Nodo {
    Attribute atributo;
    String rama;
    String valor;
    ArrayList<Nodo> hijos;

    Nodo(Attribute at) { // Árbol
        atributo = at;
        valor = "";
        hijos = new ArrayList<Nodo>();
    }

    Nodo(String val) { // Hoja
        atributo = null;
        valor = val;
        hijos = null;
    }
 
    void AgregaHijo(Nodo hijo, String val) {
        // Para llegar a hijo se necesita el valor rama
        Nodo nuevo = hijo.clone();
        nuevo.rama = val;
        hijos.add(nuevo);

    }

    Nodo eligeHijo(String val) {
        for (Nodo h : hijos) {
            // Elige el hijo cuya rama sea <val>
            if (h.rama.equals(val))
            return h;
        }
        // No debe llegar aca
        return null;
        
    }

    String Evalua(Instance dato) {
        Nodo nodo = this;
        while (!nodo.esHoja()) {
            nodo = nodo.eligeHijo(dato.stringValue(nodo.atributo));
        }
        String val = nodo.valor;

        return val;
    }

    Boolean esHoja() {
        return (hijos == null);
    }

    @Override
    public Nodo clone() {
        // Arbol o hoja?
        Nodo n;
        if (esHoja()) {
            n = new Nodo(valor);
            n.rama = rama;
        }
        else {
            n = new Nodo(atributo);
            n.rama = rama;
            n.hijos = hijos;
        }

        return n;

    }

    @Override
    public String toString() {
        String res = cadena(1);
        return res;
    }
    
    public String cadena(int p) {
        /* La tipica
            Nodo
              hijo1
              hijo2
              ...
        */ 
        String san = "";
        String sanp = ""; // Sangria previa
        for (int i = 0; i < p*2; i++) {
            san+=" ";
        }
        for (int i = 0; i < (p-1)*2; i++) {
            sanp+=" ";
        }
        String res = sanp;
        if (hijos != null) {
            res += atributo.name();
            for (Nodo h : hijos) {
                res += "\n" + san + h.rama + "\n" + sanp + " " + h.cadena(p+1);
            }
        }
        else {
            res += valor;
        }
        return res;
    }
}

