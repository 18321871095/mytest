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
'use strict';

var KISBPM = KISBPM || {};
KISBPM.TOOLBAR = {
    ACTIONS: {

        saveModel: function (services) {

            var modal = services.$modal({
                backdrop: true,
                keyboard: true,
                template: 'editor-app/popups/save-model.html?version=' + Date.now(),
                scope: services.$scope
            });
        },

        undo: function (services) {

            // Get the last commands
            var lastCommands = services.$scope.undoStack.pop();

            if (lastCommands) {
                // Add the commands to the redo stack
                services.$scope.redoStack.push(lastCommands);

                // Force refresh of selection, might be that the undo command
                // impacts properties in the selected item
                if (services.$rootScope && services.$rootScope.forceSelectionRefresh) 
                {
                	services.$rootScope.forceSelectionRefresh = true;
                }
                
                // Rollback every command
                for (var i = lastCommands.length - 1; i >= 0; --i) {
                    lastCommands[i].rollback();
                }
                
                // Update and refresh the canvas
                services.$scope.editor.handleEvents({
                    type: ORYX.CONFIG.EVENT_UNDO_ROLLBACK,
                    commands: lastCommands
                });
                
                // Update
                services.$scope.editor.getCanvas().update();
                services.$scope.editor.updateSelection();
            }
            
            var toggleUndo = false;
            if (services.$scope.undoStack.length == 0)
            {
            	toggleUndo = true;
            }
            
            var toggleRedo = false;
            if (services.$scope.redoStack.length > 0)
            {
            	toggleRedo = true;
            }

            if (toggleUndo || toggleRedo) {
                for (var i = 0; i < services.$scope.items.length; i++) {
                    var item = services.$scope.items[i];
                    if (toggleUndo && item.action === 'KISBPM.TOOLBAR.ACTIONS.undo') {
                        services.$scope.safeApply(function () {
                            item.enabled = false;
                        });
                    }
                    else if (toggleRedo && item.action === 'KISBPM.TOOLBAR.ACTIONS.redo') {
                        services.$scope.safeApply(function () {
                            item.enabled = true;
                        });
                    }
                }
            }
        },

        redo: function (services) {

            // Get the last commands from the redo stack
            var lastCommands = services.$scope.redoStack.pop();

            if (lastCommands) {
                // Add this commands to the undo stack
                services.$scope.undoStack.push(lastCommands);
                
                // Force refresh of selection, might be that the redo command
                // impacts properties in the selected item
                if (services.$rootScope && services.$rootScope.forceSelectionRefresh) 
                {
                	services.$rootScope.forceSelectionRefresh = true;
                }

                // Execute those commands
                lastCommands.each(function (command) {
                    command.execute();
                });

                // Update and refresh the canvas
                services.$scope.editor.handleEvents({
                    type: ORYX.CONFIG.EVENT_UNDO_EXECUTE,
                    commands: lastCommands
                });

                // Update
                services.$scope.editor.getCanvas().update();
                services.$scope.editor.updateSelection();
            }

            var toggleUndo = false;
            if (services.$scope.undoStack.length > 0) {
                toggleUndo = true;
            }

            var toggleRedo = false;
            if (services.$scope.redoStack.length == 0) {
                toggleRedo = true;
            }

            if (toggleUndo || toggleRedo) {
                for (var i = 0; i < services.$scope.items.length; i++) {
                    var item = services.$scope.items[i];
                    if (toggleUndo && item.action === 'KISBPM.TOOLBAR.ACTIONS.undo') {
                        services.$scope.safeApply(function () {
                            item.enabled = true;
                        });
                    }
                    else if (toggleRedo && item.action === 'KISBPM.TOOLBAR.ACTIONS.redo') {
                        services.$scope.safeApply(function () {
                            item.enabled = false;
                        });
                    }
                }
            }
        },

        cut: function (services) {
            KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editCut();
            for (var i = 0; i < services.$scope.items.length; i++) {
                var item = services.$scope.items[i];
                if (item.action === 'KISBPM.TOOLBAR.ACTIONS.paste') {
                    services.$scope.safeApply(function () {
                        item.enabled = true;
                    });
                }
            }
        },

        copy: function (services) {
            KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editCopy();
            for (var i = 0; i < services.$scope.items.length; i++) {
                var item = services.$scope.items[i];
                if (item.action === 'KISBPM.TOOLBAR.ACTIONS.paste') {
                    services.$scope.safeApply(function () {
                        item.enabled = true;
                    });
                }
            }
        },

        paste: function (services) {
            KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editPaste();
        },

        deleteItem: function (services) {
            KISBPM.TOOLBAR.ACTIONS._getOryxEditPlugin(services.$scope).editDelete();
        },

        addBendPoint: function (services) {

            var dockerPlugin = KISBPM.TOOLBAR.ACTIONS._getOryxDockerPlugin(services.$scope);

            var enableAdd = !dockerPlugin.enabledAdd();
            dockerPlugin.setEnableAdd(enableAdd);
            if (enableAdd)
            {
            	dockerPlugin.setEnableRemove(false);
            	document.body.style.cursor = 'pointer';
            }
            else
            {
            	document.body.style.cursor = 'default';
            }
        },

        removeBendPoint: function (services) {

            var dockerPlugin = KISBPM.TOOLBAR.ACTIONS._getOryxDockerPlugin(services.$scope);

            var enableRemove = !dockerPlugin.enabledRemove();
            dockerPlugin.setEnableRemove(enableRemove);
            if (enableRemove)
            {
            	dockerPlugin.setEnableAdd(false);
            	document.body.style.cursor = 'pointer';
            }
            else
            {
            	document.body.style.cursor = 'default';
            }
        },

        /**
         * Helper method: fetches the Oryx Edit plugin from the provided scope,
         * if not on the scope, it is created and put on the scope for further use.
         *
         * It's important to reuse the same EditPlugin while the same scope is active,
         * as the clipboard is stored for the whole lifetime of the scope.
         */
        _getOryxEditPlugin: function ($scope) {
            if ($scope.oryxEditPlugin === undefined || $scope.oryxEditPlugin === null) {
                $scope.oryxEditPlugin = new ORYX.Plugins.Edit($scope.editor);
            }
            return $scope.oryxEditPlugin;
        },

        zoomIn: function (services) {
            KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).zoom([1.0 + ORYX.CONFIG.ZOOM_OFFSET]);
        },

        zoomOut: function (services) {
            KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).zoom([1.0 - ORYX.CONFIG.ZOOM_OFFSET]);
        },
        
        zoomActual: function (services) {
            KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).setAFixZoomLevel(1);
        },
        
        zoomFit: function (services) {
        	KISBPM.TOOLBAR.ACTIONS._getOryxViewPlugin(services.$scope).zoomFitToModel();
        },
        
        alignVertical: function (services) {
        	KISBPM.TOOLBAR.ACTIONS._getOryxArrangmentPlugin(services.$scope).alignShapes([ORYX.CONFIG.EDITOR_ALIGN_MIDDLE]);
        },
        
        alignHorizontal: function (services) {
        	KISBPM.TOOLBAR.ACTIONS._getOryxArrangmentPlugin(services.$scope).alignShapes([ORYX.CONFIG.EDITOR_ALIGN_CENTER]);
        },
        
        sameSize: function (services) {
        	KISBPM.TOOLBAR.ACTIONS._getOryxArrangmentPlugin(services.$scope).alignShapes([ORYX.CONFIG.EDITOR_ALIGN_SIZE]);
        },
        
        closeEditor: function(services) {
        	window.location.href = "./";
        },
        
        /**
         * Helper method: fetches the Oryx View plugin from the provided scope,
         * if not on the scope, it is created and put on the scope for further use.
         */
        _getOryxViewPlugin: function ($scope) {
            if ($scope.oryxViewPlugin === undefined || $scope.oryxViewPlugin === null) {
                $scope.oryxViewPlugin = new ORYX.Plugins.View($scope.editor);
            }
            return $scope.oryxViewPlugin;
        },
        
        _getOryxArrangmentPlugin: function ($scope) {
            if ($scope.oryxArrangmentPlugin === undefined || $scope.oryxArrangmentPlugin === null) {
                $scope.oryxArrangmentPlugin = new ORYX.Plugins.Arrangement($scope.editor);
            }
            return $scope.oryxArrangmentPlugin;
        },

        _getOryxDockerPlugin: function ($scope) {
            if ($scope.oryxDockerPlugin === undefined || $scope.oryxDockerPlugin === null) {
                $scope.oryxDockerPlugin = new ORYX.Plugins.AddDocker($scope.editor);
            }
            return $scope.oryxDockerPlugin;
        }
    }
};

/** Custom controller for the save dialog */

var SaveModelCtrl = [ '$rootScope', '$scope', '$http', '$route', '$location',
    function ($rootScope, $scope, $http, $route, $location) {

        var modelMetaData = $scope.editor.getModelMetaData();

    var description = '';
    if (modelMetaData.description) {
    	description = modelMetaData.description;
    }
    
    var saveDialog = { 'name' : modelMetaData.name,
            'description' : description};
    
    $scope.saveDialog = saveDialog;
    
    var json = $scope.editor.getJSON();
    json = JSON.stringify(json);

    var params = {
        modeltype: modelMetaData.model.modelType,
        json_xml: json,
        name: 'model'
    };

    $scope.status = {
        loading: false
    };

    $scope.close = function () {
    	$scope.$hide();
    };
        var modelURL = window.location.href;

        var myid=modelURL.split("&")[2].split("=")[1];

    if(modelURL.indexOf("description")==-1){
        $http({
            method:'post',
            url:'processDiagram/getClassify',
        }).success(function(req){
            $scope.classifys=req;
           if(myid!=''){
               setTimeout(function () {
                   var sel=document.getElementById('docTextArea');
                   for(var i=0;i<sel.options.length;i++){
                       if(sel.options[i].value==myid)
                       {
                           sel.options[i].selected=true;
                           break;
                       }
                   }
               },200)
           }

        });
    }else{
        console.log('============>不发送')
    }



        var editorLayer,index;
        layui.use('layer', function(){
            editorLayer = layui.layer;
        });

        $scope.saveAndClose = function () {
            //保存成功后回调函数
    	$scope.save(function() {
            editorLayer.close(index);
            var pathName= document.location.pathname;
            var index = pathName.substr(1).indexOf("/");
            var result = pathName.substr(0,index+1);
            var orign=document.location.origin;
           // console.log(orign+ result)
            editorLayer.alert('保存成功,可在模型列表中查看',{offset:'200px',icon: 1},function(index) {
                window.parent.FS.tabPane.closeActiveTab();
                window.parent.FS.tabPane.addItem({title:"部署列表",src:orign+ result+"/"+"static/jsp/adminjsp/proDeployInfo.jsp"});
            });
            //window.parent.FS.tabPane.closeActiveTab();
           // window.parent.FS.tabPane.addItem({title:"部署列表",src:"static/jsp/adminjsp/proDeployInfo.jsp"});
        });
    };


    $scope.save = function (successCallback) {
        index = editorLayer.load(2, {offset: '200px'});
      try {
          var mingcheng = document.getElementById("nameField").value;
          var miaoshu = document.getElementById("docTextArea").value;
          var message = [];

          if (typeof(mingcheng) == 'undefined' || mingcheng == '' || typeof(miaoshu) == 'undefined' || miaoshu == '') {
              editorLayer.close(index);
              editorLayer.alert('请填写名称或描述', {offset: '200px'});
          }
          else {
              if (!$scope.saveDialog.name || $scope.saveDialog.name.length == 0) {
                  return;
              }
              // Indicator spinner image
              $scope.status = {
                  loading: true
              };

              modelMetaData.name = $scope.saveDialog.name;
              modelMetaData.description = $scope.saveDialog.description;

              var json = $scope.editor.getJSON();
              var ParallelGatewayNUM=0;
              var InclusiveGatewayNUM=0;
              var myTaskListener=[];
              var endNode=false;
              var noAssgin=[];
              var setNextAssginId=[];
              // console.log(JSON.stringify(json))
              //校验流程画的是否正确
              //校验连线是否有源头与目标
              //  var flag=true;
              for (var j = 0; j < json.childShapes.length; j++) {
                  var flowElement = json.childShapes[j];
                  var stencil_id = flowElement.stencil.id;
                  //连线
                  if ("SequenceFlow" == stencil_id) {
                      if (flowElement.outgoing.length==0) {
                          //查询连线出口线未连上元素
                          showSequenceFlowError(json, message, flowElement.resourceId);
                          break;
                      }/*else{
                          flowElement.properties["defaultflow"]="false";
                      }*/
                      /*else if (flowElement.properties.frfunction != '') {
                          //添加分支条件
                          flowElement.properties["conditionsequenceflow"] = "${" + "act_" + uuid() + "==" + "'" + "true" + "'" + "}";
                      }*/
                  }

                  else if ("UserTask" == stencil_id) {
                    //  console.log(flowElement.properties)
                      flowElement.properties.tasklisteners = "";
                      //flowElement.properties.usertaskassignment="";
                      if (flowElement.properties.name == '') {
                          message.push("流程图中的某个任务节点没有名称，请填写名称");
                          break;
                      }
                      else if(flowElement.properties.formkeydefinition==''){
                          message.push("节点名称：" + flowElement.properties.name + "的表单编号没有设置模板路径")
                          break;
                      }
                      else if (flowElement.outgoing.length == 0) {
                          message.push("节点名称：" + flowElement.properties.name + "的出口方向连线没有与该任务节点元素连上")
                          break;
                      }
                     /* else if(flowElement.properties.usertaskassignment=='' ||flowElement.properties.huiqian=='' ){
                        //  noAssgin.push(flowElement.resourceId);
                      }*/
                      //会签节点
                      else if (flowElement.properties.multiinstance_type == 'Parallel' || flowElement.properties.multiinstance_type == 'Sequential' || flowElement.properties.huiqian!='') {
                        //  console.log("会签节点"+flowElement.properties.name)
                          if (flowElement.properties.multiinstance_collection == "" || typeof (flowElement.properties.multiinstance_collection) == 'undefined') {
                              flowElement.properties["multiinstance_collection"] = "Parallel_" + uuid();
                          }
                          flowElement.properties["multiinstance_variable"] = "list";
                          flowElement.properties.usertaskassignment = {"assignment": {"assignee": "${list}"}};
                          //自动流转
                          flowElement.properties["autobypass"] = "false";
                          flowElement.properties["seccondautobypass"] = "false";
                          //转办
                          flowElement.properties["zhuanban"] = "false";
                          if(flowElement.properties.multiinstance_type == 'Sequential'){
                            //加签
                              flowElement.properties["issetaddhuiqian"] = "false";
                          }

                          if(flowElement.properties.huiqian=='j**h'){
                              flowElement.properties.huiqian='';
                          }

                      }/*else if(flowElement.properties.huiqian!=''){
                          if(flowElement.properties.multiinstance_type == 'None'){
                              message.push("节点名称：" + flowElement.properties.name + "会签节点设置了会签人，多实例类型应该选择Parallel或Sequential");
                              break;
                          }
                      }*/
                      //单任务节点
                      else if (flowElement.properties.multiinstance_type == 'None') {
                         // console.log("None:"+flowElement.properties.name)
                          flowElement.properties["multiinstance_collection"] = "";
                          flowElement.properties["multiinstance_variable"] = "";//${ads}
                          //添加会签
                          flowElement.properties["issetaddhuiqian"] = "false";
                          if(typeof(flowElement.properties.usertaskassignment.assignment)!='undefined'){
                              var value=flowElement.properties.usertaskassignment.assignment.assignee;
                              var value_cadiUser="";
                              if(typeof(flowElement.properties.usertaskassignment.assignment.candidateUsers)!='undefined'){
                                  value_cadiUser=flowElement.properties.usertaskassignment.assignment.candidateUsers[0].value;
                              }
                              if(value!=''){
                                  if(value.indexOf("$")>-1){
                                      if(value.indexOf("{")>-1 && value.indexOf("}")>-1){
                                          var param11=value.substring(value.indexOf("{")+1,value.indexOf("}"));
                                          if(param11==''){
                                              message.push("节点名称：" + flowElement.properties.name + "的办理人表达式错误，应该是${变量名}，变量名必须是字母开头")
                                          }else{
                                              var reg = /^[a-zA-Z]/;
                                              if(!reg.test(param11)){
                                                  message.push("节点名称：" + flowElement.properties.name + "的办理人表达式错误，应该是${变量名}，变量名必须是字母开头")
                                              }
                                          }

                                      }else{
                                          message.push("节点名称：" + flowElement.properties.name + "的办理人表达式错误，应该是${变量名}，变量名必须是字母开头")
                                      }
                                  }

                                  if(value.indexOf("${Assgin_")>-1 || value.indexOf("${list")>-1){
                                      flowElement.properties.usertaskassignment='';
                                  }
                              }else if(value_cadiUser!=''){
                                  //抢占式任务
                                  //自动流转
                                  flowElement.properties["autobypass"] = "false";
                                  flowElement.properties["seccondautobypass"] = "false";
                                  //转办
                                  flowElement.properties["zhuanban"] = "false";
                              }


                          }


                      }

                      //动态设置办理人(单节点或会签)
                      if (flowElement.properties.issetassgin !== '') {
                          var target1 = flowElement.outgoing[0].resourceId;
                          setNextAssginId.push(target1);
                      }

                    /*  else{
                          flowElement.properties.usertaskassignment="";
                          flowElement.properties.huiqian="";
                      }*/

                      //这里判断任务节点是否有定时开始事件
                      if (flowElement.outgoing.length == 2) {
                          setBoundary(json, flowElement, message,myTaskListener);
                      }else if (flowElement.outgoing.length >2){
                          message.push("节点名称：" + flowElement.properties.name + "的出口方向连线数量只能有一条或依附的边界定时开始元素只能一个");
                          break;
                      }

                  }

                  else if ("BoundaryTimerEvent" == stencil_id) {
                      var flagBoundaryTimerEvent = checkBoundaryTimerEvent(json, flowElement.resourceId);
                      if (!flagBoundaryTimerEvent) {
                          message.push(flowElement.properties.name + "边界定时事件并没有依附任务元素");
                          break;
                      } else {
                          if (!checkBoundaryTimerValue(flowElement, message)) {
                              break;
                          }
                      }
                  }

                  //排他网关
                  else if ("ExclusiveGateway" == stencil_id) {
                      var tiaojian = flowElement.outgoing;
                      if(tiaojian.length>1){
                          //检查条件并设置条件
                          checkTiaoJian(tiaojian, message, json)
                      }else if(tiaojian.length==1){
                          var id=tiaojian[0].resourceId;
                          for(var ii=0;ii<json.childShapes.length;ii++){
                              var flowElementii = json.childShapes[ii];
                              if(flowElementii.resourceId==id){
                                  flowElementii.properties.frfunction="";
                                  flowElementii.properties.conditionsequenceflow="";
                              }
                          }
                      }

                  }

                  //并行网关
                  else if("ParallelGateway" == stencil_id){
                      ParallelGatewayNUM++;
                  }

                  //包容网关
                  else if("InclusiveGateway" == stencil_id){
                      InclusiveGatewayNUM++;
                      var tiaojian1 = flowElement.outgoing;
                      if(tiaojian1.length>1){
                          //检查条件并设置条件
                          checkTiaoJian(tiaojian1, message, json)
                         /* if (!checkTiaoJian(tiaojian1, message, json)) {
                              message.push("包容他网关(出口线大于两条)出口的所有连线都要写条件");
                          }*/
                      }else if(tiaojian1.length==1){
                          var id1=tiaojian1[0].resourceId;
                          for(var iii=0;iii<json.childShapes.length;iii++){
                              var flowElementiii = json.childShapes[iii];
                              if(flowElementiii.resourceId==id1){
                                  flowElementiii.properties.frfunction="";
                                  flowElementiii.properties.conditionsequenceflow="";
                              }
                          }
                      }
                  }


                  else if ("StartNoneEvent" == stencil_id) {
                      // console.log("添加开始节点监听器")
                      if (!checkStartNodeValue(flowElement, message)) {
                          break;
                      }else if(flowElement.outgoing.length>1){
                          message.push("开始节点出口连线数量只能是1条");
                          break;
                      }
                      else {
                          flowElement.properties["executionlisteners"] = {
                              "executionListeners": [{
                                  "event": "start",
                                  "implementation": "com.fr.tw.util.progressLisenter",
                                  "className": "com.fr.tw.util.progressLisenter",
                                  "expression": "",
                                  "delegateExpression": ""
                              }]
                          };
                      }
                  }

                  else if("EndNoneEvent" == stencil_id){
                      endNode=true;
                  }

              }

              if(ParallelGatewayNUM%2!=0){
                  message.push("并行网关必须成对出线");
              }
              if(InclusiveGatewayNUM%2!=0){
                  message.push("包容网关必须成对出线");
              }
              if(!endNode){
                  message.push("整个流程图没有结束节点");
              }
              //循环完后添加监听器
              //console.log("添加任务监听器");
              //console.log(myTaskListener)
              for(var mm=0;mm<myTaskListener.length;mm++){
                  var task=myTaskListener[mm]["task"]; var task1=myTaskListener[mm]["task1"];
                  for(var jj=0;jj<json.childShapes.length;jj++){
                      var flowElement1 = json.childShapes[jj];
                      var resourceId1 = flowElement1.resourceId;
                      if(task==resourceId1){
                          console.log(flowElement1.properties.name)
                          flowElement1.properties["tasklisteners"]={
                              "taskListeners": [{
                                  "event": "create",
                                  "implementation": "com.fr.tw.util.userTaskLisenter",
                                  "className": "com.fr.tw.util.userTaskLisenter",
                                  "expression": "",
                                  "delegateExpression": ""
                              }]
                          };
                      }
                      if(task1==resourceId1){
                          console.log(flowElement1.properties.name)
                          flowElement1.properties["tasklisteners"]=
                              {
                                  "taskListeners": [{
                                      "event": "create",
                                      "implementation": "com.fr.tw.util.userTasksendMessageLisenter",
                                      "className": "com.fr.tw.util.userTasksendMessageLisenter",
                                      "expression": "",
                                      "delegateExpression": ""
                                  }]
                              };
                      }
                  }
              }

              for(var kk=0;kk<setNextAssginId.length;kk++){
                 var param = checkSequenceFlow(json, setNextAssginId[kk]);
                 if (param != '0' && param != '1') {
                     //动态设置办理人(单节点或会签)
                     setNextAssginParam(json, param);
                 } else if (param == '0') {
                     message.push("节点名称：" + flowElement.properties.name + "的出口方向连线没有连上所对应的元素");
                     break;
                 } else if (param == '1') {
                    // message.push("节点名称：" + flowElement.properties.name + "的动态设置下个办理人的下个节点必须是任务节点类型");
                     break;
                 }
              }

              //console.log(json.childShapes);

              //console.log("判断是否指定办理人");
              for(var aa=0;aa<json.childShapes.length;aa++){
                  var flowElement11 = json.childShapes[aa];
                  var resourceId11 = flowElement11.stencil.id;

                  if("UserTask"==resourceId11){
                      //console.log(flowElement11)
                      if(flowElement11.properties.multiinstance_type=='None'){
                        if(flowElement11.properties.usertaskassignment==''){
                            message.push("节点名称：" + flowElement11.properties.name + "的添加办理人栏位为空")
                            break;
                        }
                        if(flowElement11.properties.huiqian!=''){
                            message.push("节点名称：" + flowElement11.properties.name + "的节点类型是None，" +
                                "添加会签人栏位应该为空")
                            break;
                        }

                      }else{
                          if(flowElement11.properties.huiqian=='' ){
                              message.push("节点名称：" + flowElement11.properties.name + "的添加会签人栏位为空")
                              break;
                          }else{
                              if(flowElement11.properties.multiinstance_type=='None'){
                                  message.push("节点名称：" + flowElement11.properties.name + "的添加会签人栏位有值，" +
                                      "节点类型应该选择Parallel或Sequential")
                                  break;
                              }

                          }
                      }

/*
                      if(flowElement11.properties.usertaskassignment==''){
                          message.push("节点名称：" + flowElement.properties.name + "没有指定办理人")
                          break;
                      }
                      if(flowElement11.properties.multiinstance_type!='None' || flowElement11.properties.multiinstance_type!='None'){

                      }*/
                  }
              }

              if (message.length === 0) {
                  json = JSON.stringify(json);
                  var selection = $scope.editor.getSelection();
                  $scope.editor.setSelection([]);
                  // Get the serialized svg image source
                  var svgClone = $scope.editor.getCanvas().getSVGRepresentation(true);
                  $scope.editor.setSelection(selection);
                  if ($scope.editor.getCanvas().properties["oryx-showstripableelements"] === false) {
                      var stripOutArray = jQuery(svgClone).find(".stripable-element");
                      for (var i = stripOutArray.length - 1; i >= 0; i--) {
                          stripOutArray[i].remove();
                      }
                  }
                  // Remove all forced stripable elements
                  var stripOutArray = jQuery(svgClone).find(".stripable-element-force");
                  for (var i = stripOutArray.length - 1; i >= 0; i--) {
                      stripOutArray[i].remove();
                  }

                  // Parse dom to string
                  var svgDOM = DataManager.serialize(svgClone);
                  svgDOM = svgDOM.replace(/marker-start="url\("#/g, "marker-start=\"url(#").replace(/start"\)"/g, "start\)\"");
                  svgDOM = svgDOM.replace(/marker-mid="url\("#/g, "marker-mid=\"url(#").replace(/mid"\)"/g, "mid\)\"");
                  svgDOM = svgDOM.replace(/marker-end="url\("#/g, "marker-end=\"url(#").replace(/end"\)"/g, "end\)\"");

                  var params = {
                      json_xml: json,
                      svg_xml: svgDOM,
                      name: $scope.saveDialog.name,
                      description: $scope.saveDialog.description
                  };

                  // Update

                  $http({
                      method: 'PUT',
                      data: params,
                      ignoreErrors: true,
                      headers: {
                          'Accept': 'application/json',
                          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                      },
                      transformRequest: function (obj) {
                          var str = [];
                          for (var p in obj) {
                              str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                          }
                          return str.join("&");
                      },
                      url: KISBPM.URL.putModel(modelMetaData.modelId)
                  })

                      .success(function (data, status, headers, config) {
                          $scope.editor.handleEvents({
                              type: ORYX.CONFIG.EVENT_SAVED
                          });
                          $scope.modelData.name = $scope.saveDialog.name;
                          $scope.modelData.lastUpdated = data.lastUpdated;

                          $scope.status.loading = false;
                          $scope.$hide();

                          // Fire event to all who is listening
                          var saveEvent = {
                              type: KISBPM.eventBus.EVENT_TYPE_MODEL_SAVED,
                              model: params,
                              modelId: modelMetaData.modelId,
                              eventType: 'update-model'
                          };
                          KISBPM.eventBus.dispatch(KISBPM.eventBus.EVENT_TYPE_MODEL_SAVED, saveEvent);

                          // Reset state
                          $scope.error = undefined;
                          $scope.status.loading = false;
                          editorLayer.close(index);
                          // Execute any callback
                          if (successCallback) {
                              successCallback();
                          }
                      })
                      .error(function (data, status, headers, config) {
                          $scope.error = {};
                          console.log('Something went wrong when updating the process model:' + JSON.stringify(data));
                          $scope.status.loading = false;
                      });

              }

              else {
                  editorLayer.close(index);
                  editorLayer.alert(message[0], {offset: '200px', icon: 2});
                  $scope.$hide();
              }

          };
      }
      catch (err){
          editorLayer.close(index);
          editorLayer.alert("页面脚本代码发生错误请联系开发人员，错误信息："+err, {offset: '200px', icon: 2});
          $scope.$hide();
      }


        }//$scope.save



}];
function uuid() {
    return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
        return v.toString(16);
    });
}



function showSequenceFlowError(json,message,id) {
    for(var mm=0;mm<json.childShapes.length;mm++){
        var out=json.childShapes[mm].outgoing;
        for(var k=0;k<out.length;k++){
            if(out[k].resourceId==id){
                message.push("节点名称："+json.childShapes[mm].properties.name+",类型："+
                    getType(json.childShapes[mm].stencil.id)+"的出口方向连线没有连上所对应的元素")
                break;
            }
        }
    }
}

function checkSequenceFlow(json,target) {
    var flag1="";
    for(var j=0;j<json.childShapes.length;j++) {
        var flowElement = json.childShapes[j];
        var stencil_id = flowElement.stencil.id;
        //连线
        if ("SequenceFlow" == stencil_id && flowElement.resourceId==target) {
            if (typeof(flowElement.target) == 'undefined') {
                flag1="0";
                break;
            }else{
                flag1=flowElement.target.resourceId;
                for(var k=0;k<json.childShapes.length;k++) {
                    var flowElement1 = json.childShapes[k];
                    var stencil_id1 = flowElement1.stencil.id;
                    if(flowElement1.resourceId==flag1){
                        if("UserTask"!=stencil_id1){
                            flag1="1";
                        }
                        break;
                    }
                }
                break;
            }
        }
    }
    return flag1;
}

function setNextAssginParam(json,target) {
    for(var j=0;j<json.childShapes.length;j++) {
        var flowElement = json.childShapes[j];
        var stencil_id = flowElement.stencil.id;
        //连线
        if ("UserTask" == stencil_id && flowElement.resourceId==target) {
           // console.log(flowElement)
            if(flowElement.properties.multiinstance_type=='Parallel' || flowElement.properties.multiinstance_type=='Sequential'){
                if(flowElement.properties.huiqian==''){
                    flowElement.properties.huiqian="j**h";
                }
                break;
            }else{
                if(typeof(flowElement.properties.usertaskassignment.assignment)!='undefined'){


                }else{
                    flowElement.properties.usertaskassignment={"assignment":{"assignee":"${Assgin_"+uuid()+"}"}};
                }

                break;
            }
        }
    }
}

function setBoundary(json,flowElement,message,myTaskListener) {
    //只可能是2，其中一个是正常出线，另一个是边界定时事件
    var temp={};
    temp["task"]=flowElement.resourceId;
   /* flowElement.properties["tasklisteners"]={
        "taskListeners": [{
            "event": "create",
            "implementation": "com.fr.tw.util.userTaskLisenter",
            "className": "com.fr.tw.util.userTaskLisenter",
            "expression": "",
            "delegateExpression": ""
        }]
    };*/

    var boundaryTimerEventId = flowElement.outgoing[1].resourceId;
    //重新循环遍历
    for(var j=0;j<json.childShapes.length;j++){
        var flowElement1 = json.childShapes[j];
        var resourceId1 = flowElement1.resourceId;
        if(boundaryTimerEventId==resourceId1){
            if(flowElement1.stencil.id=='SequenceFlow'){
                message.push("节点名称：" + flowElement.properties.name + "的出口方向连线数量只能有一条");
                break;
            }else{
                var targetid="";
                if(typeof(flowElement1.outgoing[0])!='undefined'){
                    //边界定时事件连线的id
                    targetid =flowElement1.outgoing[0] .resourceId;
                    // console.log("resourceId1:"+targetid)
                    for(var m=0;m<json.childShapes.length;m++){
                        var flowElement2 = json.childShapes[m];
                        var resourceId2 = flowElement2.resourceId;
                        if(resourceId2==targetid){
                            //targetid1是边界定时事件的目标任务节点，这里是不能有分支的
                            var targetid1 = "";
                            if(typeof(flowElement2.outgoing[0])!='undefined'){
                                targetid1 = flowElement2.outgoing[0].resourceId;
                                //边界定时事件连线所指向的任务节点id
                                // console.log("resourceId2:"+targetid1)
                                for(var k=0;k<json.childShapes.length;k++){
                                    var flowElement3 = json.childShapes[k];
                                    var resourceId3 = flowElement3.resourceId;
                                    if(targetid1==resourceId3){
                                        if("UserTask"==flowElement3.stencil.id){
                                            temp["task1"]=resourceId3;
                                            myTaskListener.push(temp);
                                            /* flowElement3.properties["tasklisteners"]=
                                                 {
                                                     "taskListeners": [{
                                                         "event": "create",
                                                         "implementation": "com.fr.tw.util.userTasksendMessageLisenter",
                                                         "className": "com.fr.tw.util.userTasksendMessageLisenter",
                                                         "expression": "",
                                                         "delegateExpression": ""
                                                     }]
                                                 };*/
                                        }else if("EndNoneEvent"==flowElement3.stencil.id){

                                        }
                                        else{
                                            message.push("边界定时开始元素出口连线对应元素只能是任务节点");
                                            break;
                                        }
                                    }
                                }

                            }else{
                                message.push("边界定时开始元素出口连线没有对应元素");
                                break;
                            }
                        }
                    }
                }else{
                    message.push("边界定时开始元素没有对应连线元素");
                    break;
                }
            }
        }
    }

}

function checkBoundaryTimerEvent(json,target) {
    var flag2=false;
    for(var j=0;j<json.childShapes.length;j++) {
        var flowElement = json.childShapes[j];
        var stencil_id = flowElement.stencil.id;
        if ("UserTask" == stencil_id && flowElement.outgoing.length==2){
            if(flowElement.outgoing[1].resourceId==target){
                flag2=true;
                break;
            }
        }
    }
    return flag2;
}

function checkBoundaryTimerValue(flowElement,message) {
    var flag3=true;var a=false; var b=false;
    //超期时间
    var time =  flowElement.properties.timerdurationdefinition;
    //逾期时间
    var overdue =  flowElement.properties.overdue;
    if(time==''){
        message.push(flowElement.properties.name+"边界定时事件没有设置到期时间");
    }else {
        if((time.indexOf("PT")>-1)  && (time.indexOf("S")>-1 || time.indexOf("M")>-1 || time.indexOf("H")>-1) && (time.indexOf("D")<0) ){
            a=true;
        }else if((time.indexOf("P")>-1) && (time.indexOf("D")>-1) && (time.indexOf("T")<0) ){
            a=true;
        }else{
            flag3=false;
            message.push(flowElement.properties.name+"边界定时事件的到期时间格式错误，" +
                "应该是1秒：PT1S，1分钟：PT1M，1小时：PT1H，1天：P1D。注意天数是P，不是PT，字母必须大写。");
        }
    }
    if(overdue!=''){
        if((overdue.indexOf("PT")>-1) && (overdue.indexOf("S")>-1 || overdue.indexOf("M")>-1 || overdue.indexOf("H")>-1) && (time.indexOf("D")<0) ){
            b=true;
        }else if((overdue.indexOf("P")>-1) && (overdue.indexOf("D")>-1)  && (time.indexOf("T")<0) ){
            b=true;
        }else{
            flag3=false;
            message.push(flowElement.properties.name+"边界定时事件的逾期时间格式错误，" +
                "应该是1秒：PT1S，1分钟：PT1M，1小时：PT1H，1天：P1D。注意天数是P，不是PT，字母必须大写。");
        }
    }
    if(a && b){
        if(!checkTimeValue(time,overdue)){
            message.push(flowElement.properties.name+"边界定时事件的到期时间小于逾期时间");
        }

    }
    return flag3;
}

function checkStartNodeValue(flowElement,message) {
    var flag4=true;
    var processtime =  flowElement.properties.processtime;
    if(processtime!=''){
        if((processtime.indexOf("PT")>-1) && (processtime.indexOf("S")>-1 || processtime.indexOf("M")>-1 || processtime.indexOf("H")>-1) && (processtime.indexOf("D")<0) ){

        }else if((processtime.indexOf("P")>-1) && (processtime.indexOf("D")>-1)  && (processtime.indexOf("T")<0) ){

        }else{
            flag4=false;
            message.push("开始节点的流程期限时间格式错误，" +
                "应该是1秒：PT1S，1分钟：PT1M，1小时：PT1H，1天：P1D。注意天数是P，不是PT，字母必须大写。");
        }
    }
    return flag4;
}

function checkTimeValue(time1,time2) {
    //time1>time2 true
    var flag=true;
    if(time1.indexOf("d")>-1 || time1.indexOf("D")>-1){
        if(time2.indexOf("d")>-1 || time2.indexOf("D")>-1){
            if(parseInt(getNumInStr(time1))<parseInt(getNumInStr(time2))){
                flag = false;
            }
        }else if(time2.indexOf("h")>-1 || time2.indexOf("H")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)/24)){
                flag = false;
            }
        }
        else if(time2.indexOf("m")>-1 || time2.indexOf("M")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)/60/24)){
                flag = false;
            }
        }
        else if(time2.indexOf("s")>-1 || time2.indexOf("S")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)/60/60/24)){
                flag = false;
            }
        }
    }
    else if(time1.indexOf("h")>-1 || time1.indexOf("H")>-1){
        if(time2.indexOf("d")>-1 || time2.indexOf("D")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)*24)){
                flag = false;
            }
        }else if(time2.indexOf("h")>-1 || time2.indexOf("H")>-1){
            if(parseInt(getNumInStr(time1))<parseInt(getNumInStr(time2))){
                flag = false;
            }
        }
        else if(time2.indexOf("m")>-1 || time2.indexOf("M")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)/60)){
                flag = false;
            }
        }
        else if(time2.indexOf("s")>-1 || time2.indexOf("S")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)/60/60)){
                flag = false;
            }
        }
    }
    else if(time1.indexOf("m")>-1 || time1.indexOf("M")>-1){
        if(time2.indexOf("d")>-1 || time2.indexOf("D")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)*24*60)){
                flag = false;
            }
        }
        else if(time2.indexOf("h")>-1 || time2.indexOf("H")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)*60)){
                flag = false;
            }
        }
        else if(time2.indexOf("m")>-1 || time2.indexOf("M")>-1){
            if(parseInt(getNumInStr(time1))<parseInt(getNumInStr(time2))){
                flag = false;
            }
        }
        else if(time2.indexOf("s")>-1 || time2.indexOf("S")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)/60)){
                flag = false;
            }
        }
    }
    else if(time1.indexOf("s")>-1 || time1.indexOf("S")>-1){
        if(time2.indexOf("d")>-1 || time2.indexOf("D")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)*24*60*60)){
                flag = false;
            }
        }
        else if(time2.indexOf("h")>-1 || time2.indexOf("H")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)*60*60)){
                flag = false;
            }
        }
        else if(time2.indexOf("m")>-1 || time2.indexOf("M")>-1){
            if(parseFloat(getNumInStr(time1))<parseFloat(getNumInStr(time2)*60)){
                flag = false;
            }
        }
        else if(time2.indexOf("s")>-1 || time2.indexOf("S")>-1){
            if(parseInt(getNumInStr(time1))<parseInt(getNumInStr(time2))){
                flag = false;
            }
        }
    }

    return flag;

}

function getNumInStr(s) {
    return s.replace(/[^0-9]/ig,"");
}

function checkTiaoJian(tiaojian,message,json) {
   // var flag=true;
    //console.log(tiaojian)
    for(var i=0;i<tiaojian.length;i++){
        var id=tiaojian[i].resourceId;
        for(var j=0;j<json.childShapes.length;j++) {
            var flowElement = json.childShapes[j];
            var stencil_id = flowElement.resourceId;
            if(stencil_id==id){
               // console.log(flowElement.properties.defaultflow)
                if(flowElement.properties.defaultflow==true){
                    //console.log("有默认流")
                    flowElement.properties["conditionsequenceflow"]="";
                   if(flowElement.properties.frfunction!=''){
                       message.push("默认流不能填写条件,条件为："+flowElement.properties.frfunction);
                   }
                }else{
                    //console.log("没有默认流")
                  //  console.log(flowElement.properties.frfunction)
                  //  console.log(flowElement.properties.frfunction=='')
                    if(flowElement.properties.frfunction==''){
                        message.push("排他网关(出口线大于两条)出口的所有连线都要写条件");
                        break;
                    }else{
                        flowElement.properties["conditionsequenceflow"] = "${" + "act_" + uuid() + "==" + "'" + "true" + "'" + "}";
                    }
                }

            }

        }
    }
   // return flag;
}

function getType(text) {
    if("ExclusiveGateway"==text){
        return "排他网关";
    }else if("ParallelGateway"==text){
        return "并行网关";
    }
    else if("InclusiveGateway"==text){
        return "包容网关";
    }else if("UserTask"==text){
        return "任务节点";
    }else if("StartNoneEvent"==text){
        return "开始节点";
    }
}


