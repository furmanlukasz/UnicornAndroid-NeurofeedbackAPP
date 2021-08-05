precision mediump float;
//mediump vec4 gl_FragCoord;
varying vec4 v_Color;
uniform float[500] u_EEG1;
uniform float[500] u_EEG2;
uniform float[500] u_EEG3;
uniform float[325] u_EEG4;
uniform float[325] u_EEG5;
uniform float[325] u_EEG6;
uniform float[1024] u_EEG7;
uniform float[500] u_EEG8;
uniform sampler2D uTexture;
uniform float myValue;

float plot(vec2 st, float pct){
    return  smoothstep( pct-0.04, pct, st.y) -
    smoothstep( pct, pct+0.04, st.y);
}

float plot1(float st, float pct){
    return  smoothstep( pct-0.04, pct, st) -
    smoothstep( pct, pct+0.04, st);
}


void main(){
    vec2 res = vec2(1200.0,297.0);
    vec2 st = gl_FragCoord.xy/res;
    int ipos = int(floor(st.x*500.0));

    float pct1 = plot1(st.y,abs(u_EEG1[ipos]));
    float pct2 = plot1(st.y,abs(u_EEG2[ipos]));
    //float pct3 = plot1(st.y,abs(u_EEG3[ipos]));
    //float pct1 = plot1(st.y,abs(u_EEG1[ipos]));
    //float temp = abs(u_EEG8[ipos]);

    vec3 color = pct1*vec3(0.0,1.0,0.0);
    color += pct2*vec3(1.0,0.0,0.0);
    //color += pct3*vec3(0.0,0.0,1.0);
    gl_FragColor = vec4(color,1.0);
}