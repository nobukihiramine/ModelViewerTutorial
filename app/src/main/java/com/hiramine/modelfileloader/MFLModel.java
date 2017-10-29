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

public class MFLModel
{
	// メンバー変数
	public int                  iCountVertex;        // 頂点
	public float[]              af3Vertex;
	public int                  iCountNormal;        // 法線
	public float[]              af3Normal;
	public int                  iCountTriangle;    // 三角形
	public MFLIndexedTriangle[] aIndexedTriangle;
	public MFLMaterial[]        aMaterial;            // マテリアル
	public int                  iCountGroup;        // グループ
	public MFLGroup             groupRoot;

	public MFLGroup addGroup( String strName )
	{
		MFLGroup group_work = findGroup( strName );
		if( null == group_work )
		{
			group_work = new MFLGroup( strName );
			group_work.groupNext = groupRoot;
			groupRoot = group_work;
			++iCountGroup;
		}

		return group_work;
	}

	public MFLGroup findGroup( String strName )
	{
		MFLGroup group_work = groupRoot;
		while( null != group_work )
		{
			if( strName.equals( group_work.strName ) )
			{
				break;
			}
			group_work = group_work.groupNext;
		}
		return group_work;
	}

	public int findMaterial( String strName )
	{
		int iIndexMaterial;
		for( iIndexMaterial = 0; iIndexMaterial < aMaterial.length; ++iIndexMaterial )
		{
			if( strName.equals( aMaterial[iIndexMaterial].strName ) )
			{
				break;
			}
		}
		if( aMaterial.length == iIndexMaterial )
		{ // 見つからなかった場合には、"(NoName)"マテリアルにする。
			return 0;
		}
		return iIndexMaterial;
	}
}
