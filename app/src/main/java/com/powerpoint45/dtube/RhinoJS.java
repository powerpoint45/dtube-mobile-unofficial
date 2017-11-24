package com.powerpoint45.dtube;

import android.content.Context;

/**
 * Created by michael on 23/11/17.
 */

public class RhinoJS {

    Context c;

    public RhinoJS(Context c){
        this.c = c;
    }


//    public void loadRhino(){
//        RhinoAndroidHelper helper = new RhinoAndroidHelper(c);
//        org.mozilla.javascript.Context jc = helper.enterContext();
//        Scriptable globalScope = jc.initStandardObjects();
//        jc.setOptimizationLevel(-1);
//        Reader steem = null;
//        Reader bluebird = null;
//        Reader dateFormat = null;
//        Reader dtube = null;
//        try {
//            steem  = new BufferedReader(new InputStreamReader(c.getResources().openRawResource(R.raw.steem),"UTF8"));
//            bluebird = new BufferedReader(new InputStreamReader(c.getResources().openRawResource(R.raw.bluebird),"UTF8"));
//            dateFormat = new BufferedReader(new InputStreamReader(c.getResources().openRawResource(R.raw.dateformat),"UTF8"));
//            dtube = new BufferedReader(new InputStreamReader(c.getResources().openRawResource(R.raw.dtube),"UTF8"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            jc.evaluateReader(globalScope, steem, "steem.js", 0, null);
//            jc.evaluateReader(globalScope, bluebird, "bluebird.js", 0, null);
//            jc.evaluateReader(globalScope, dateFormat, "dateformat.js", 0, null);
//            jc.evaluateReader(globalScope, dtube, "dtube.js", 0, null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Add a global variable out that is a JavaScript reflection of the System.out variable:
//        Object wrappedOut = jc.javaToJS(System.out, globalScope);
//        ScriptableObject.putProperty(globalScope, "out", wrappedOut);
//
//        String code = "steem.api.getFollowing('immawake', 0, \"blog\", 100, function(err, result) {\n" +
//                "\t\tvar users = [];\n" +
//                "\t\tfor (u =0; u<result.length; u++){\n" +
//                "\t\t\tusers.push(result[u].following);\n" +
//                "\t\t}\n" +
//                "\t\tout.print(users);\n" +
//                "\t});";
//
//
//
//        // The module esprima is available as a global object due to the same
//        // scope object passed for evaluation:
//        jc.evaluateString(globalScope, code, "<mem>", 1, null);
//        jc.exit();
//
//    }


}
