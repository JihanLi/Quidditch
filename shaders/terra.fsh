varying vec3 lightDir, normal, view;
varying float height, offset0, offset1;

uniform sampler2D grass, dirt, snow;
uniform float low, mid, high;

void main()
{
	vec3 ambient = gl_LightModel.ambient.rgb + gl_LightSource[0].ambient.rgb;
	vec3 diffuse = gl_LightSource[0].diffuse.rgb;
	vec3 specular = gl_LightSource[0].specular.rgb;
	
	float range = 200.0;
	float delta = 20.0;
	
	float pLow, pMid, pHigh, pSum;
	float dLow, dMid, dHigh;
	
	dLow = height - low - offset1 + delta - range;
	if (dLow < delta)
	{
		dLow = delta;
	}
	
	dMid = abs(height - mid) + delta - range;
	if (dMid < delta)
	{
		dMid = delta;
	}
	
	dHigh = high + offset0 - height + delta - range;
	if (dHigh < delta)
	{
		dHigh = delta;
	}
	
	pLow = pMid = pHigh = 0.0;
	
	pLow = pow(dMid * dHigh, 2.0);
	pMid = pow(dLow * dHigh, 2.0);
	pHigh = pow(dLow * dMid, 2.0);
	
	pSum = pLow + pMid + pHigh;
	
	pLow /= pSum;
	pMid /= pSum;
	pHigh /= pSum;
	
	vec3 halfVector = normalize(lightDir + view);
	
	float ndotl = max(dot(normal, lightDir), 0.0);
	float ndothv = max(dot(normal, halfVector), 0.0);
	
	vec3 cl = ambient;
	
	cl += ndotl * diffuse;
	cl += specular * pow(ndothv, gl_FrontMaterial.shininess) * pHigh;
	
	vec4 texGrass = texture2D(grass, gl_TexCoord[0].st);
	vec4 texDirt = texture2D(dirt, gl_TexCoord[0].st);
	vec4 texSnow = texture2D(snow, gl_TexCoord[0].st);
	
	vec3 ct = pLow * texGrass.rgb + pMid * texDirt.rgb + pHigh * texSnow.rgb;
	vec3 color = cl * ct;
	
	gl_FragColor = vec4(color, 1.0);
}