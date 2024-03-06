/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pkg1718_p2si;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author fidel
 */
public class Adaboost {
    static ArrayList[] imgs;
    static ArrayList entrenamiento;
    static ArrayList test;
    static int digitoActual=-1;
    static float minErrorPesos=1;

    
    static int[] resulAdaboost= new int[10];
    static int altoRnd;
    static int anchoRnd;
    static int humbralDir;
    static int humbralValue;
    
    static int[] cdAltoRnd = new int[1000];
    static int[] cdAnchoRnd = new int[1000];
    static int[] cdHumbralDir = new int[1000];
    static int[] cdHumbralValue = new int[1000];
    
    static float[] pesos;

    static float sumaErrores;

    static float Z=0;
    static float[] a;
    static int[] h=new int[1000];
    static float[] D=new float[1000];

    
    public static void cargarImagenes(int porcentaje) {
        //Cargador MNIST de SI
        MNISTLoader ml = new MNISTLoader();
        ml.loadDBFromPath("./mnist_1000");
        
        int numFotosTest;

        imgs = new ArrayList[10];
        entrenamiento = new ArrayList<Imagen>();
        test = new ArrayList<Imagen>();
        //Accedo a las imagenes de dÃ­gito 1
        for (int i=0;i<10;i++) {
            imgs[i] = ml.getImageDatabaseForDigit(i);
            
            numFotosTest=(int)(imgs[i].size()*((float)porcentaje/100));
            for (int j=0;j<imgs[i].size();j++) {
                Imagen nimg=(Imagen) imgs[i].get(i);
                nimg.imagen=i;
                nimg.imagen=i;
                if (j>numFotosTest) {
                    test.add(nimg);
                }
                else {
                    entrenamiento.add(nimg);
                }
            }
        }
        pesos=new float[entrenamiento.size()];
        for (int j=0;j<entrenamiento.size();j++) {
            Imagen nimg=(Imagen)entrenamiento.get(j);
            nimg.peso=1/(float)entrenamiento.size();
            entrenamiento.set(j, nimg);
            pesos[j]=1/(float)entrenamiento.size();
        }
        for (int j=0;j<entrenamiento.size();j++) {
            Imagen nimg=(Imagen)entrenamiento.get(j);
            //System.out.println("Num"+j+" digit:"+nimg.imagen+" peso:"+nimg.peso);
        }
        //System.out.println("ytamaño test"+test.size()+"tam entrenamiento"+entrenamiento.size());
    }
    public static void darPesos() {
        int numFotosTest=entrenamiento.size();
        for (int i=0;i<numFotosTest;i++) {
            //D[i] = 1/(float)numFotosTest;
        }
    }
    public static void crearClasificadorDebil(int dimension) {
        Random rand = new Random();
        anchoRnd=rand.nextInt(dimension);
        altoRnd=rand.nextInt(dimension);
        humbralDir=rand.nextInt(2);
        humbralValue=(rand.nextInt(255))-127;
        
        //Positivo sea mayor, Negativo sea menor
        if (humbralDir==0)
            humbralDir=-1;
        
    }
    public static float entrenamientoYerror() {
        //para cada img del test
        sumaErrores=0;
        Imagen img;
        for (int i=0;i<entrenamiento.size();i++) {
            img = (Imagen) entrenamiento.get(i);
            byte imageData[] = img.getImageData();

            if (digitoActual!=img.imagen) {
                if (((humbralDir==1 && (imageData[anchoRnd*altoRnd])>humbralValue) || (humbralDir==-1 && (imageData[anchoRnd*altoRnd])<humbralValue)))
                {
                    sumaErrores = sumaErrores+pesos[i];
                }
            }
            else {
                if (!((humbralDir==1 && (imageData[anchoRnd*altoRnd])>humbralValue) || (humbralDir==-1 && (imageData[anchoRnd*altoRnd])<humbralValue)))
                {
                    sumaErrores = sumaErrores+pesos[i];
                }
            }
            //System.out.println("la suma es "+sumaErrores+" "+img.peso);
        }
        return sumaErrores;
    }
    public static void generarConfianza(int nClasificador) {
        //float naux=(1-minErrorPesos)/minErrorPesos;
        //float aux = (float)Math.log10(naux)/(float)Math.log10(2);
        float aux = (float)Math.log((float)((float)1.0-(float)minErrorPesos)/(float)minErrorPesos);
        a[digitoActual]=aux;
        //System.out.println(a[digitoActual]+" para "+nClasificador+" con minerror pesos "+minErrorPesos);
    }
    public static void guardarClasificador(int nClasificador) {
        cdAnchoRnd[nClasificador]=anchoRnd;
        cdAltoRnd[nClasificador]=altoRnd;
        cdHumbralDir[nClasificador]=humbralDir;
        cdHumbralValue[nClasificador]=humbralValue;
    }
    public static void actualizarD(int t, int nClasificadores) {
        
        for (int i=0;i<entrenamiento.size();i++) {
            Imagen img = (Imagen) entrenamiento.get(i);
            float pesoImgOrig=img.peso;
            D[i]=(float) ((float)pesos[i]*Math.exp(-1*a[digitoActual]*sacarY(i)*sacarH(i,t)));
            //System.out.println(pesoImgOrig+"<-peso "+D[i]+"="+pesos[i]+"*"+Math.E+"^("+(-1)+"*"+a[t]+"*"+sacarY(i)+"*"+sacarH(i,t)+"/"+Z);
        }
        for (int i=0;i<entrenamiento.size();i++){
            //System.out.println("ppesos "+".."+D[i]);
        }
        Z=0;
        for (int i=0;i<entrenamiento.size();i++) {
            Z=D[i]+Z;
        }
        for (int i=0;i<entrenamiento.size();i++) {
            D[i]=D[i]/Z;
            Imagen img = (Imagen) entrenamiento.get(i);
            img.peso=D[i];
            entrenamiento.set(i, img);
        }


        /*
        float aux=0;
        for (int i=0;i<entrenamiento.size();i++) {
            Imagen img = (Imagen) entrenamiento.get(i);
            System.out.println(img.peso);
            aux=img.peso+aux;
        }
        System.out.println("aaa"+aux);*/
        
    }
    public static float sacarY(int nImagen) {
        Imagen img = (Imagen) entrenamiento.get(nImagen);

        return img.imagen;
    }
    public static float sacarH(int nImagen, int nClasificadores) {
        Imagen img = (Imagen) entrenamiento.get(nImagen);
        byte imageData[] = img.getImageData();

        int salida=0;
        
        if (digitoActual!=img.imagen) {
            if (!((cdHumbralDir[nClasificadores]==1 && (imageData[cdAnchoRnd[nClasificadores]*cdAltoRnd[nClasificadores]])>cdHumbralValue[nClasificadores]) || (cdHumbralDir[nClasificadores]==-1 && (imageData[cdAnchoRnd[nClasificadores]*cdAltoRnd[nClasificadores]])<cdHumbralValue[nClasificadores])))
            {
                salida+=1;
            }
        }
        else if (digitoActual==img.imagen){
            if (((cdHumbralDir[nClasificadores]==1 && (imageData[cdAnchoRnd[nClasificadores]*cdAltoRnd[nClasificadores]])>cdHumbralValue[nClasificadores]) || (cdHumbralDir[nClasificadores]==-1 && (imageData[cdAnchoRnd[nClasificadores]*cdAltoRnd[nClasificadores]])<cdHumbralValue[nClasificadores])))
            {
                salida+=1;
            }
        }
        else {
            salida+=-1;
        }
        if (salida>0)
            salida=1;
        else
            salida=-1;
        
        h[nImagen]=salida;
        return salida;
    }
    static public int sacarHfinal(int nClasificadores) {
        
        float auxH=0;
        
        for (int i=0;i<nClasificadores;i++) {
            auxH=((a[digitoActual]*htest[i])+auxH);
            //System.out.println("auxH history  "+auxH);
        }
        
        if (auxH<=0) {
            return 1;
        }
        else{
            return -1;
        }
    }
    static int[] htest;
    public static void aplicarTest(int nClasificadores, int nImgTest) {
        
        int salida=0;
        htest=new int[nClasificadores];
        
        Imagen img = (Imagen) test.get(nImgTest);
        byte imageData[] = img.getImageData();

        salida=0;

        for (int i=0;i<nClasificadores;i++){
            if (digitoActual!=img.imagen) {

            }
            else if (digitoActual==img.imagen){
                if (((cdHumbralDir[i]==1 && (imageData[cdAnchoRnd[i]*cdAltoRnd[i]])>cdHumbralValue[i]) || (cdHumbralDir[i]==-1 && (imageData[cdAnchoRnd[i]*cdAltoRnd[i]])<cdHumbralValue[i])))
                {
                    salida=1;
                }
            }
            else {
                salida=-1;
            }
            htest[i]=salida;
        }
    }
    public static int Adaboost(int numActual, int Porcentaje, int T, int imgTest) throws IOException {
        digitoActual = numActual;
        darPesos();

        minErrorPesos=1;
        for (int t=0;t<T;t++) {
            //el mejor clasificador debil
            for (int i=0;(minErrorPesos>0.5);i++) {
                //dimension imagenes
                crearClasificadorDebil(28);
                //System.out.println(entrenamientoYerror()+"--"+minErrorPesos);
                minErrorPesos=entrenamientoYerror();
                guardarClasificador(t);
                if (minErrorPesos<0.5) {
                    //System.out.println(digitoActual+"|"+cdHumbralDir[t]+"|"+cdHumbralValue[t]+"|"+cdAnchoRnd[t]+"|"+cdAltoRnd[t]+"\n");
                    if (opcion==1)
                        volcarFichero(digitoActual+"|"+cdHumbralDir[t]+"|"+cdHumbralValue[t]+"|"+cdAnchoRnd[t]+"|"+cdAltoRnd[t]+"\n");
                    break;
                }
            }
            generarConfianza(t);
            actualizarD(t,T);
        }
        
        aplicarTest(T,imgTest);
             
        
        //for (int i=0;i<T;i++)
            //System.out.println("el error es "+minErrorPesos+"la a es "+a[i]);
        
        return sacarHfinal(T);
    }
    public static void volcarFichero(String cadena) throws IOException {

        FileWriter fw=null;
        try {
            File file = new File(ruta);
            fw = new FileWriter(file.getAbsoluteFile(), true);

            fw.write(cadena);
        }
        catch (Exception e) {}
        finally {
            fw.close();
        }
    }
    public static void leerFichero(String ruta, int imgTest) throws IOException {

        File fichero = new File(ruta);
        Scanner s = null;
        String contenido="";
        try {
            // Leemos el contenido del fichero
            System.out.println("... Leemos el contenido del fichero ...");
            s = new Scanner(fichero);

            while (s.hasNextLine()) {
                contenido += s.nextLine();
            }
        } catch (Exception ex) {
            System.out.println("Mensaje: " + ex.getMessage());
        } finally {
            try {
                if (s != null)
                    s.close();
            } catch (Exception ex2) {
                //System.out.println("Mensaje 2: " + ex2.getMessage());
            }
        }

        String lineas[]=contenido.split("\r\n");
        
        for (int i=0;i<lineas.length;i++) {
            String parametros[]=lineas[i].split("|");
            //for (int j=0;j<parametros.length;j++)
                //System.out.println(parametros[j]);
            if (parametros.length>=4) {
                digitoActual=Integer.parseInt(parametros[0]);
                cdHumbralDir[i]=Integer.parseInt(parametros[2]);
                cdHumbralValue[i]=Integer.parseInt(parametros[4]);
                cdAltoRnd[i]=Integer.parseInt(parametros[6]);
                cdAnchoRnd[i]=Integer.parseInt(parametros[8]);
            }
            generarConfianza(i);
            actualizarD(i,lineas.length);
            aplicarTest(lineas.length,imgTest);
            resulAdaboost[digitoActual=Integer.parseInt(parametros[0])]=sacarHfinal(lineas.length);
        }
    }
    static int opcion=-1;
    static String ruta;
    public static void main(String[] args) throws IOException {
        int Nclasificadores=180;
        a=new float[10];
        cargarImagenes(80);
        
        if (args.length>0) {
            if (args[0].equals("-t")){
                if (args.length==2) {
                    opcion=1;
                    ruta=args[1];
                }
            }
            else{
                ruta=args[0];
                opcion=2;
            }
        }
        if (opcion==1) {
            File file = new File(ruta);
            file.delete();
            //volcarFichero(args[0]);
        }
        else if (opcion==2){
            leerFichero(ruta, 1);
        }
        
        final long startTime = System.currentTimeMillis();
        //Digito y porcentaje y N clasificadores, IMAGEN TEST
        int imagenTestNumero;
        float confianzaMenor=0;
        int num=-1;
        int aciertos=0;
        int fallos=0;
                
        for (int k=0;k<test.size();k++) {
            imagenTestNumero=k;

            if (opcion!=2) {
                for (int i=0;i<10;i++) {
                    resulAdaboost[i]=Adaboost(i, 20, Nclasificadores, imagenTestNumero);
                }
            }
            Imagen img=(Imagen)test.get(imagenTestNumero);
            //System.out.println("La img nº"+imagenTestNumero+" que en realidad es un "+img.imagen+":");
            for (int i=0;i<10;i++) {
                if (resulAdaboost[i]==1) {
                    if (i==img.imagen) {
                        aciertos++;
                        break;
                    }
                }
            }
        }
        
        System.out.println("N aciertos "+aciertos+", N fallos"+(test.size()-aciertos));
        final long endTime = System.currentTimeMillis();
        //System.out.println("Tiempo ejecucion 10 Adboost 250 clasificadores debiles por Adaboost: " + (endTime - startTime) );

    }
}
