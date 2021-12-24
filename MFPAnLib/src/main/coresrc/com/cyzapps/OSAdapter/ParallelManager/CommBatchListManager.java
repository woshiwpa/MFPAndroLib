// MFP project, CommBatchListManager.java : Designed and developed by Tony Cui in 2021
package com.cyzapps.OSAdapter.ParallelManager;

import java.util.ArrayList;

public class CommBatchListManager {
	class IndexCallCommPack {
		public int index;
		public CallCommPack callCommPack;
		public IndexCallCommPack(int idx) {
			index = idx;
			callCommPack = null;
		}
		public IndexCallCommPack(CallCommPack pack) {
			index = pack.index;
			callCommPack = pack;
		}
	}
    ArrayList<IndexCallCommPack> commBatchList = new ArrayList<IndexCallCommPack>();

    public void add(CallCommPack callCommPack2Add) {
    	int idx;
    	for (idx = 0; idx < commBatchList.size(); idx ++) {
    		if (commBatchList.get(idx).index == callCommPack2Add.index) {
    			commBatchList.get(idx).callCommPack = callCommPack2Add;	// ok, it is filled.
    			break;
    		}
    	}
    	if (idx == commBatchList.size()) {
    		while (true) {
    			if (commBatchList.size() == 0) {
    				commBatchList.add(new IndexCallCommPack(0));
    			} else {
    				commBatchList.add(new IndexCallCommPack(commBatchList.get(commBatchList.size() - 1).index + 1));
    			}
    			if (commBatchList.get(idx).index == callCommPack2Add.index) {
    				commBatchList.get(idx).callCommPack = callCommPack2Add;
    				break;
    			}
    			idx ++;
    		}
    	}
    	// remove continuous node.
    	while (1 < commBatchList.size()) {
    		if (commBatchList.get(0).callCommPack != null && commBatchList.get(1).callCommPack != null) {
    			commBatchList.remove(0);	// ok, let's remove it..
    		} else {
    			break;
    		}
    	}
    }
    
    public int size() {
    	return commBatchList.size();
    }
    
    public int getLastIndex() {
    	if (commBatchList.size() == 0) {
    		return -1;
    	} else {
    		return commBatchList.get(commBatchList.size() - 1).index;
    	}
    }
    
    public CallCommPack getLastCallCommPack() {
    	if (commBatchList.size() == 0) {
    		return null;
    	} else {
    		return commBatchList.get(commBatchList.size() - 1).callCommPack;
    	}
    }
}
