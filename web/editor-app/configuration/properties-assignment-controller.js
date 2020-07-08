/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * Assignment
 */
var KisBpmAssignmentCtrl = [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/assignment-popup.html?version=' + Date.now(),
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmAssignmentPopupCtrl = [ '$scope', function($scope) {
    // Put json representing assignment on scope
    //{candidateUsers: Array(1), assignee: ""}
   // console.log($scope.property.value.assignment )
    if ($scope.property.value !== undefined && $scope.property.value !== null
        && $scope.property.value.assignment !== undefined
        && $scope.property.value.assignment !== null) 
    {
       //console.log($scope.property.value.assignment.assignee)
        var assginValue=$scope.property.value.assignment.assignee;

        if(assginValue=='${list}' || assginValue.indexOf("${Assgin_")>-1){
            $scope.property.value.assignment.assignee="";
            $scope.assignment = $scope.property.value.assignment;

        }else{
            $scope.assignment = $scope.property.value.assignment;

        }
    } else {
       // console.log('没有值')
        $scope.assignment = {};
    }

    if ($scope.assignment.candidateUsers == undefined || $scope.assignment.candidateUsers.length == 0)
    {  //console.log('candidateUsers没有值')
        $scope.assignment.candidateUsers = [{value: ''}];
    }

    // Click handler for + button after enum value
   /* var userValueIndex = 1;
    $scope.addCandidateUserValue = function(index) {
        $scope.assignment.candidateUsers.splice(index + 1, 0, {value: 'value ' + userValueIndex++});
    };*/

    // Click handler for - button after enum value
  /*  $scope.removeCandidateUserValue = function(index) {
        $scope.assignment.candidateUsers.splice(index, 1);
    };*/

    /* if ($scope.assignment.candidateGroups == undefined || $scope.assignment.candidateGroups.length == 0)
    {
    	$scope.assignment.candidateGroups = [{value: ''}];
    }
    
   var groupValueIndex = 1;
    $scope.addCandidateGroupValue = function(index) {
        $scope.assignment.candidateGroups.splice(index + 1, 0, {value: 'value ' + groupValueIndex++});
    };

    // Click handler for - button after enum value
    $scope.removeCandidateGroupValue = function(index) {
        $scope.assignment.candidateGroups.splice(index, 1);
    };*/

   /* $scope.$emit("jh","我是办理人节点");
    $scope.$on("jh1",function (e,msg) {
        console.log("办理人子节点收到:"+msg)

    })
    console.log("办理人子节:")*/

    $scope.save = function() {//猜测 $scope.assignment是子页面值， $scope.property.value.assignment是保存后显示的值
        var assign=document.getElementById("assigneeField").value.replace(/\s|\xA0/g,"");
        var users=document.getElementById("userField").value.replace(/\s|\xA0/g,"");
        if(assign!='' || users!=''){
            //console.log("assign || users  不为空")
            $scope.property.value = {};
            //handleAssignmentInput($scope);
            //$scope.property.value.assignment = $scope.assignment;
            //自定义
            //var group_arr=[];
           // var group_temp={};
           // group_temp["value"]=users;
          //  group_arr.push(group_temp)
            $scope.assignment.assignee=assign;
            $scope.assignment.candidateUsers[0].value=users;
            //end
            $scope.property.value.assignment= $scope.assignment;

           // console.log($scope.assignment)assignment.candidateUsers
            //[{\"value\": \"Tom\", \"$$hashKey\": \"06S\"}]"
            //console.log(JSON.stringify($scope.assignment.candidateUsers))
            //console.log("保存："+JSON.stringify($scope.property))
        }else{
            //console.log("assign || users  都为空")
            $scope.property.value = "";
        }

		$scope.updatePropertyInModel($scope.property);
        $scope.close();
    };
    //自定义
    $scope.showAssign = function() {
		document.getElementById("AssignDiv").style.display="block";
        document.getElementById("GroupDiv").style.display="none";
        document.getElementById("userField").value="";

    };
    $scope.showGroup = function() {
        document.getElementById("AssignDiv").style.display="none";
        document.getElementById("GroupDiv").style.display="block";
        document.getElementById("assigneeField").value="";

    };
    //end
    // Close button handler
    $scope.close = function() {
    	//handleAssignmentInput($scope);
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };
    
    var handleAssignmentInput = function($scope) {
    	if ($scope.assignment.candidateUsers)
    	{
	    	var emptyUsers = true;
	    	var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateUsers.length; i++)
	        {
	        	if ($scope.assignment.candidateUsers[i].value != '')
	        	{
	        		emptyUsers = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }
	        
	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateUsers.splice(toRemoveIndexes[i], 1);
	        }
	        
	        if (emptyUsers)
	        {
	        	$scope.assignment.candidateUsers = undefined;
	        }
    	}
        
    	/*if ($scope.assignment.candidateGroups)
    	{
	        var emptyGroups = true;
	        var toRemoveIndexes = [];
	        for (var i = 0; i < $scope.assignment.candidateGroups.length; i++)
	        {
	        	if ($scope.assignment.candidateGroups[i].value != '')
	        	{
	        		emptyGroups = false;
	        	}
	        	else
	        	{
	        		toRemoveIndexes[toRemoveIndexes.length] = i;
	        	}
	        }
	        
	        for (var i = 0; i < toRemoveIndexes.length; i++)
	        {
	        	$scope.assignment.candidateGroups.splice(toRemoveIndexes[i], 1);
	        }
	        
	        if (emptyGroups)
	        {
	        	$scope.assignment.candidateGroups = undefined;
	        }
    	}*/
    };
}];