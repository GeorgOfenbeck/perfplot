/*
 Copyright (c) 2012, Intel Corporation
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
// written by Austen Ott
//    
#include <IOKit/IOLib.h>
#include <IOKit/IOKitKeys.h>
#include <libkern/OSByteOrder.h>
#include "PcmMsrClient.h"

#define super IOUserClient

OSDefineMetaClassAndStructors(com_intel_driver_PcmMsrClient, IOUserClient)

const IOExternalMethodDispatch PcmMsrClientClassName::sMethods[kNumberOfMethods] = {
    { (IOExternalMethodAction) &PcmMsrClientClassName::sOpenDriver, 0, 0, 0, 0},
    { (IOExternalMethodAction) &PcmMsrClientClassName::sCloseDriver, 0, 0, 0, 0},
    { (IOExternalMethodAction) &PcmMsrClientClassName::sReadMSR, 0, kIOUCVariableStructureSize, 0, kIOUCVariableStructureSize},
    { (IOExternalMethodAction) &PcmMsrClientClassName::sWriteMSR, 0, kIOUCVariableStructureSize, 0, 0},
    { (IOExternalMethodAction) &PcmMsrClientClassName::sBuildTopology, 0, 0, 0, kIOUCVariableStructureSize},
    { (IOExternalMethodAction) &PcmMsrClientClassName::sGetNumInstances, 0, 0, 1, 0},
    { (IOExternalMethodAction) &PcmMsrClientClassName::sIncrementNumInstances, 0, 0, 1, 0},
    { (IOExternalMethodAction) &PcmMsrClientClassName::sDecrementNumInstances, 0, 0, 1, 0}
};

IOReturn PcmMsrClientClassName::externalMethod(uint32_t selector, IOExternalMethodArguments* args,													IOExternalMethodDispatch* dispatch, OSObject* target, void* reference)
{
    if (selector < (uint32_t) kNumberOfMethods) {
        dispatch = (IOExternalMethodDispatch *) &sMethods[selector];
        
        if (!target) {
			target = this;
		}
    }
	
	return super::externalMethod(selector, args, dispatch, target, reference);
}

bool PcmMsrClientClassName::start(IOService* provider)
{
	bool result = false;
    
    fProvider = OSDynamicCast(PcmMsrDriverClassName, provider);
    
    if (fProvider != NULL) {
		result = super::start(provider);
	}
    else
 		IOLog("PcmMsrClientClassName::start failed.\n");
        
    return result;
}

IOReturn PcmMsrClientClassName::clientClose(void)
{    
    closeUserClient();
    
	if (!terminate()) {
		IOLog("PcmMsrClientClassName::clientClose failed.\n");
	}
	
    return kIOReturnSuccess;
}

bool PcmMsrClientClassName::didTerminate(IOService* provider, IOOptionBits options, bool* defer)
{	
	closeUserClient();
	*defer = false;
	
	return super::didTerminate(provider, options, defer);
}


IOReturn PcmMsrClientClassName::sOpenDriver(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* arguments)
{
    return target->openUserClient();
}

IOReturn PcmMsrClientClassName::openUserClient(void)
{
    IOReturn	result = kIOReturnSuccess;
    
    if (fProvider == NULL || isInactive()) {
        result = kIOReturnNotAttached;
		IOLog("%s::%s returned kIOReturnNotAttached.\n", getName(), __FUNCTION__);
	} else if (!fProvider->open(this)) {
		result = kIOReturnExclusiveAccess;
		IOLog("%s::%s returned kIOReturnExclusiveAccess.\n", getName(), __FUNCTION__);
	}
	
    return result;
}

IOReturn PcmMsrClientClassName::checkActiveAndOpened (const char* memberFunction)
{
    if (fProvider == NULL || isInactive()) {
        IOLog("%s::%s returned kIOReturnNotAttached.\n", getName(), memberFunction);
        return kIOReturnNotAttached;
        
    } else if (!fProvider->isOpen(this)) {
        IOLog("%s::%s returned kIOReturnNotOpen.\n", getName(), memberFunction);
        return  kIOReturnNotOpen;
    }
    return kIOReturnSuccess;
}


IOReturn PcmMsrClientClassName::sCloseDriver(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* arguments)
{
    return target->closeUserClient();
}

IOReturn PcmMsrClientClassName::closeUserClient(void)
{
    IOReturn	result = checkActiveAndOpened (__FUNCTION__);
    
    if (result == kIOReturnSuccess)
 		fProvider->close(this);

    return result;
}

IOReturn PcmMsrClientClassName::sReadMSR(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* arguments){
    return target->readMSR((pcm_msr_data_t*) arguments->structureInput, (pcm_msr_data_t*) arguments->structureOutput);
}

IOReturn PcmMsrClientClassName::readMSR(pcm_msr_data_t* idata, pcm_msr_data_t* odata)
{
    IOReturn	result = checkActiveAndOpened (__FUNCTION__);
    
    if (result == kIOReturnSuccess)
 		result = fProvider->readMSR(idata, odata);
    
    return result;
}

IOReturn PcmMsrClientClassName::sWriteMSR(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* arguments){
    return target -> writeMSR((pcm_msr_data_t*)arguments->structureInput);
}

IOReturn PcmMsrClientClassName::writeMSR(pcm_msr_data_t* data)
{
    IOReturn	result = checkActiveAndOpened (__FUNCTION__);
    
    if (result == kIOReturnSuccess)
		result = fProvider->writeMSR(data);
    
    return result;
}

IOReturn PcmMsrClientClassName::sBuildTopology(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* args){
    return target -> buildTopology((topologyEntry*)args->structureOutput, args->structureOutputSize);
}

IOReturn PcmMsrClientClassName::buildTopology(topologyEntry* data, size_t output_size)
{
    uint32_t num_cores = output_size / sizeof(topologyEntry);
    IOReturn	result = checkActiveAndOpened (__FUNCTION__);
    
    if (result == kIOReturnSuccess)
		result = fProvider->buildTopology(data, num_cores);
    
    return result;
}

IOReturn PcmMsrClientClassName::sGetNumInstances(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* args){
    return target->getNumInstances((uint32_t*)&args->scalarOutput[0]);
}
IOReturn PcmMsrClientClassName::getNumInstances(uint32_t* num_insts){
    return fProvider->getNumInstances(num_insts);
}

IOReturn PcmMsrClientClassName::sIncrementNumInstances(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* args){
    return target->incrementNumInstances((uint32_t*)&args->scalarOutput[0]);
}
IOReturn PcmMsrClientClassName::incrementNumInstances(uint32_t* num_insts){
    return fProvider->incrementNumInstances(num_insts);
}

IOReturn PcmMsrClientClassName::sDecrementNumInstances(PcmMsrClientClassName* target, void* reference, IOExternalMethodArguments* args){
    return target->decrementNumInstances((uint32_t*)&args->scalarOutput[0]);
}
IOReturn PcmMsrClientClassName::decrementNumInstances(uint32_t* num_insts){
    return fProvider->decrementNumInstances(num_insts);
}
