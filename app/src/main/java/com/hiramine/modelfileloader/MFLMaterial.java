/*
 * Copyright 2017 Nobuki HIRAMINE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiramine.modelfileloader;

public class MFLMaterial
{
	// メンバー変数
	public String  strName    = "";                // 名前
	public float[] f4Ambient  = { 0.2f, 0.2f, 0.2f, 1.0f };
	public float[] f4Diffuse  = { 0.8f, 0.8f, 0.8f, 1.0f };
	public float[] f4Specular = { 0.0f, 0.0f, 0.0f, 1.0f };
	public float   fShininess = 0.0f;
}
