﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class NetWorkTest : MonoBehaviour {

    public Text text;
	// Use this for initialization
	void Start () {
        text.text = Application.internetReachability.ToString();
	}
	
	// Update is called once per frame
	void Update () {
		
	}
}
