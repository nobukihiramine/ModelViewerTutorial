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
package com.hiramine.modelviewertutorial;

import com.hiramine.modelfileloader.MFLGroup;
import com.hiramine.modelfileloader.MFLIndexedTriangle;
import com.hiramine.modelfileloader.MFLMaterial;
import com.hiramine.modelfileloader.MFLModel;
import com.hiramine.modelfileloader.ObjFileLoader;
import com.hiramine.modelfileloader.OnProgressListener;
import com.hiramine.modelfileloader.StlFileLoader;

public class ModelFileLoader
{
	public static Model load( String strPath, OnProgressListener onProgressListener )
	{
		MFLModel mflmodel = null;

		// 拡張子ごとの処理
		String strPath_lowercase = strPath.toLowerCase();
		if( strPath_lowercase.endsWith( ".stl" ) )
		{
			mflmodel = StlFileLoader.load( strPath, onProgressListener );
		}
		else if( strPath_lowercase.endsWith( ".obj" ) )
		{
			mflmodel = ObjFileLoader.load( strPath, onProgressListener );
		}

		// MFLModel => Model 変換
		return MFLModel2Model( mflmodel );
	}

	private static Model MFLModel2Model( MFLModel mflmodel )
	{
		if( null == mflmodel )
		{
			return null;
		}

		int      iCountGroup   = 0;
		MFLGroup mflgroup_work = mflmodel.groupRoot;
		while( null != mflgroup_work )
		{
			if( null == mflgroup_work.aiIndexTriangle )
			{
				mflgroup_work = mflgroup_work.groupNext;
				continue;
			}
			++iCountGroup;
			mflgroup_work = mflgroup_work.groupNext;
		}
		Group[] aGroup = new Group[iCountGroup];

		int iIndexGroup = 0;
		mflgroup_work = mflmodel.groupRoot;
		while( null != mflgroup_work )
		{
			if( null == mflgroup_work.aiIndexTriangle )
			{
				mflgroup_work = mflgroup_work.groupNext;
				continue;
			}

			int     iCountTriangle = mflgroup_work.aiIndexTriangle.length;
			int     iCountVertex   = iCountTriangle * 3;
			float[] af3Vertex      = new float[iCountVertex * 3];
			float[] af3Normal      = null;
			if( null != mflmodel.af3Normal )
			{
				af3Normal = new float[iCountVertex * 3];
			}

			int iIndexTriangle;
			int iIndexVertex;
			int iIndexNormal;
			int iIndex3;
			for( iIndexTriangle = 0; iIndexTriangle < iCountTriangle; ++iIndexTriangle )
			{
				MFLIndexedTriangle mflindexedtriangle = mflmodel.aIndexedTriangle[mflgroup_work.aiIndexTriangle[iIndexTriangle]];
				for( iIndex3 = 0; iIndex3 < 3; ++iIndex3 )
				{
					iIndexVertex = mflindexedtriangle.i3IndexVertex[iIndex3];
					iIndexNormal = mflindexedtriangle.i3IndexNormal[iIndex3];
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 0] = mflmodel.af3Vertex[iIndexVertex * 3 + 0];
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 1] = mflmodel.af3Vertex[iIndexVertex * 3 + 1];
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 2] = mflmodel.af3Vertex[iIndexVertex * 3 + 2];
					if( null != af3Normal )
					{
						af3Normal[iIndexTriangle * 9 + iIndex3 * 3 + 0] = mflmodel.af3Normal[iIndexNormal * 3 + 0];
						af3Normal[iIndexTriangle * 9 + iIndex3 * 3 + 1] = mflmodel.af3Normal[iIndexNormal * 3 + 1];
						af3Normal[iIndexTriangle * 9 + iIndex3 * 3 + 2] = mflmodel.af3Normal[iIndexNormal * 3 + 2];
					}
				}
			}

			Material material = null;
			if( null != mflmodel.aMaterial )
			{
				MFLMaterial mflmaterial = mflmodel.aMaterial[mflgroup_work.iIndexMaterial];
				material = new Material( mflmaterial.f4Ambient, mflmaterial.f4Diffuse, mflmaterial.f4Specular, mflmaterial.fShininess );
			}

			aGroup[iIndexGroup] = new Group( mflgroup_work.strName, af3Vertex, af3Normal, material );
			++iIndexGroup;
			mflgroup_work = mflgroup_work.groupNext;
		}

		return new Model( aGroup );
	}
}
