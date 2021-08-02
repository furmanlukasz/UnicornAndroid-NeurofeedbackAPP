precision mediump float;
//mediump vec4 gl_FragCoord;
varying vec4 v_Color;
uniform float[750] u_Data;
uniform float myValue;


float plot(vec2 st, float pct){
    return  smoothstep( pct-0.04, pct, st.y) -
    smoothstep( pct, pct+0.04, st.y);
}

void main(){
    vec2 res = vec2(350.0,297.0);
    vec2 st = gl_FragCoord.xy / res;
    st.x *= res.x/1000.0;
    vec2 ipos = floor(st * 750.0);

    float array = u_Data[int(ipos.x)]*0.00001;
    float pct = plot(st,abs(array));

    vec3 color = pct*vec3(0.0,1.0,0.0);
    gl_FragColor = vec4(color,1.0);
}