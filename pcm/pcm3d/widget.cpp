/*
Copyright (c) 2009-2012, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
// written by Roman Dementiev
//

#include "widget.h"
#include "ui_widget.h"
#include <QtGui/QMouseEvent>
#include <QTimer>
#include "../cpucounters.h"
#include <vector>

static SystemCounterState sysstate;
static std::vector<SocketCounterState> sockstate;
static std::vector<CoreCounterState> corestate;

static SystemCounterState oldsysstate;
static std::vector<SocketCounterState> oldsockstate;
static std::vector<CoreCounterState> oldcorestate;

static void readCounters()
{
  PCM * m = PCM::getInstance();
  sysstate = getSystemCounterState();
  sockstate[0] = getSocketCounterState(0);
  sockstate[1] = getSocketCounterState(1);
  for(uint32 i=0;i<m->getNumCores();++i)
    corestate[i] = getCoreCounterState(i);
}

float coreUtil[2][16];
float iMCUtil[2] = {0,0};
float QPIUtil[2] = {0,0};
float PackagePower[2] = {0,0};
float DRAMPower[2] = {0,0};

static void updateKPIs()
{
  int sock_it[2] = {0,0};
  
  PCM * m = PCM::getInstance();
  
  std::swap(sysstate,oldsysstate);
  std::swap(sockstate,oldsockstate);
  std::swap(corestate,oldcorestate);
  
  readCounters();
  
  for(uint32 i=0;i<m->getNumCores();++i)
  {
    int sock = m->getSocketId(i);
    
    // derive metric from max IPS
    coreUtil[sock][sock_it[sock]] = double(getInstructionsRetired(oldcorestate[i],corestate[i]))/ (double(m->getNominalFrequency())*double(m->getMaxIPC()));
    ++(sock_it[sock]);
  }
  
  float maxMemBW = 40.*1024.*1024.*1024.; // not precise
  
  for(uint32 i=0;i<m->getNumSockets();++i)
    iMCUtil[i] = double(getBytesReadFromMC(oldsockstate[i],sockstate[i])+getBytesWrittenToMC(oldsockstate[i],sockstate[i]))/maxMemBW;
  
  
  float maxQPIBW = 2.*8.*1024.*1024.*1024.;
  
  // link 0
  QPIUtil[0] = double(getIncomingQPILinkBytes(0,0,oldsysstate,sysstate) + getIncomingQPILinkBytes(1,0,oldsysstate,sysstate))/maxQPIBW;
  
  // link 0
  QPIUtil[1] = double(getIncomingQPILinkBytes(0,1,oldsysstate,sysstate) + getIncomingQPILinkBytes(1,1,oldsysstate,sysstate))/maxQPIBW;
  
  float maxPower = 95.; // max TDP
  float maxDRAMPower = 40.; // max TDP, just for reference, no exact value is known...
  
  for(uint32 i=0;i<m->getNumSockets();++i)
  {
    PackagePower[i] = double(getConsumedJoules(oldsockstate[i],sockstate[i]))/maxPower;
    DRAMPower[i] = double(getDRAMConsumedJoules(oldsockstate[i],sockstate[i]))/maxDRAMPower;
  }
  
}

Widget::Widget(QWidget *parent) :
    QGLWidget(parent),
    ui(new Ui::Widget)
{
    xRot = 40.0;
    yRot = 20.0;
    zRot = 0.0;
    scale = 0.3;
    
    sockstate.resize(2);
    corestate.resize(32);
    oldsockstate.resize(2);
    oldcorestate.resize(32);
    
    readCounters();
    
    ui->setupUi(this);

    QTimer *timer = new QTimer(this);
    connect(timer, SIGNAL(timeout()), this, SLOT(valueUpdate()));
    timer->start(1000);
}

void Widget::valueUpdate()
{
    updateKPIs();
    update();
}

void Widget::initializeGL()
{
        qglClearColor( Qt::darkGray );
        glShadeModel( GL_FLAT );
        glEnable(GL_DEPTH_TEST);
}

void Widget::resizeGL(int w, int h)
{
        glViewport( 0, 0, (GLint)w, (GLint)h );
        glMatrixMode( GL_PROJECTION );
        glLoadIdentity();
        glFrustum( -1.0, 1.0, -1.0, 1.0, 5.0, 15.0 );
        glMatrixMode( GL_MODELVIEW );
}


void Widget::paintGL()
{
    glClear( GL_COLOR_BUFFER_BIT| GL_DEPTH_BUFFER_BIT);

    glLoadIdentity();
    //glTranslatef( -1.0, 0.0, -10.0 );
    glTranslatef( 0, 0.0, -10.0 );
    glScalef( scale, scale, scale );

    glRotatef( xRot, 1.0, 0.0, 0.0 );
    glRotatef( yRot, 0.0, 1.0, 0.0 );
    glRotatef( zRot, 0.0, 0.0, 1.0 );

    drawAll();
}


void Widget::keyPressEvent(QKeyEvent* event)
{
    switch(event->key()) {
    case Qt::Key_Escape:
        close();
        break;
    default:
        event->ignore();
        break;
    }
}

Widget::~Widget()
{
    makeCurrent();
    delete ui;
    
    PCM::getInstance()->cleanup();
}

void Widget::drawWireFrame(float x, float y, float z)
{
    glPushMatrix();

    // wire frame
    qglColor( Qt::white );           // Shorthand for glColor3f or glIndex

    glLineWidth( 1.0 );

    glBegin( GL_LINE_LOOP );
    glVertex3f(  0.5*x,  0.5*y, 0.5*z );
    glVertex3f(  0.5*x, -0.5*y, 0.5*z );
    glVertex3f( -0.5*x, -0.5*y, 0.5*z );
    glVertex3f( -0.5*x,  0.5*y, 0.5*z );
    glEnd();


    glBegin( GL_LINE_LOOP );
    glVertex3f(  0.5*x,  0.5*y, -0.5*z );
    glVertex3f(  0.5*x, -0.5*y, -0.5*z );
    glVertex3f( -0.5*x, -0.5*y, -0.5*z );
    glVertex3f( -0.5*x,  0.5*y, -0.5*z );
    glEnd();

    glBegin( GL_LINES );
    glVertex3f(  0.5*x,  0.5*y, 0.5*z );   glVertex3f(  0.5*x,  0.5*y, -0.5*z );
    glVertex3f(  0.5*x, -0.5*y, 0.5*z );   glVertex3f(  0.5*x, -0.5*y, -0.5*z );
    glVertex3f( -0.5*x, -0.5*y, 0.5*z );   glVertex3f( -0.5*x, -0.5*y, -0.5*z );
    glVertex3f( -0.5*x,  0.5*y, 0.5*z );   glVertex3f( -0.5*x,  0.5*y, -0.5*z );
    glEnd();

    glPopMatrix();
}

void Widget::draw3DBox(float x, float y, float z, const QColor & c)
{
    glPushMatrix();
    glBegin(GL_POLYGON);
    qglColor(c);

    /*      This is the top face*/
    glVertex3f(0.0f*x, 0.0f*y, 0.0f*z);
    glVertex3f(0.0f*x, 0.0f*y, 1.0f*z);
    glVertex3f(1.0f*x, 0.0f*y, 1.0f*z);
    glVertex3f(1.0f*x, 0.0f*y, 0.0f*z);

    /*      This is the front face*/
    glVertex3f(0.0f*x, 0.0f*y, 0.0f*z);
    glVertex3f(1.0f*x, 0.0f*y, 0.0f*z);
    glVertex3f(1.0f*x, 1.0f*y, 0.0f*z);
    glVertex3f(0.0f*x, 1.0f*y, 0.0f*z);

    /*      This is the right face*/
    glVertex3f(0.0f*x, 0.0f*y, 0.0f*z);
    glVertex3f(0.0f*x, 1.0f*y, 0.0f*z);
    glVertex3f(0.0f*x, 1.0f*y, 1.0f*z);
    glVertex3f(0.0f*x, 0.0f*y, 1.0f*z);

    /*      This is the left face*/
    glVertex3f(1.0f*x, 0.0f*y, 0.0f*z);
    glVertex3f(1.0f*x, 0.0f*y, 1.0f*z);
    glVertex3f(1.0f*x, 1.0f*y, 1.0f*z);
    glVertex3f(1.0f*x, 1.0f*y, 0.0f*z);

    /*      This is the bottom face*/
    glVertex3f(0.0f*x, 0.0f*y, 0.0f*z);
    glVertex3f(0.0f*x, 1.0f*y, 1.0f*z);
    glVertex3f(1.0f*x, 1.0f*y, 1.0f*z);
    glVertex3f(1.0f*x, 1.0f*y, 0.0f*z);

    /*      This is the back face*/
    glVertex3f(0.0f*x, 0.0f*y, 0.0f*z);
    glVertex3f(1.0f*x, 0.0f*y, 1.0f*z);
    glVertex3f(1.0f*x, 1.0f*y, 1.0f*z);
    glVertex3f(0.0f*x, 1.0f*y, 1.0f*z);

    glEnd();

    glTranslatef(0.5*x,0.5*y,0.5*z);

    drawWireFrame(x,y,z);

    glPopMatrix();
}

void Widget::drawPipe(float x, float y, float z, float fill,const QColor & c)
{
    glPushMatrix();
    draw3DBox(x,fill*y,z,c);
    glTranslatef(0.5*x,0.5*y,0.5*z);
    drawWireFrame(x,y,z);
    glPopMatrix();
}

#define CORE_UNIT (0.5)
#define PLATE_UNIT (0.3)
#define CPU_WIDTH (CORE_UNIT+CORE_UNIT*4*1.3)

void Widget::drawCPU(float * height, QColor * /* colors*/, float power)
{
    glPushMatrix();

    if(power>1.0) power = 1.0; // cap
    
    QColor plate_color(255.*power,0,255.*(1.-power)); // power: 0.0 - blue, 1.0 - red

    
    draw3DBox   (CPU_WIDTH, PLATE_UNIT, CORE_UNIT+CORE_UNIT*4*1.3        , plate_color);
    glTranslatef(CORE_UNIT*0.5, PLATE_UNIT, CORE_UNIT*1.3*(4.-0.5));
    
    for(int j=0;j<4;++j)
    {
        for(int i=0;i<4;++i)
        {
            draw3DBox(CORE_UNIT, 2.*CORE_UNIT*height[j*4 + i], CORE_UNIT, Qt::blue);
            glTranslatef(0.0,0.0,-CORE_UNIT*1.3);
        }
        glTranslatef(CORE_UNIT*1.3,0, CORE_UNIT*1.3*4);
    }

    glPopMatrix();
}

#define DIMM_UNIT (CORE_UNIT*0.1)
#define DIMMS_WIDTH (9.*DIMM_UNIT*3.)

void Widget::drawDIMMMs(float power)
{
    glPushMatrix();
    
    if(power>1.0) power = 1.0; // cap
      
    QColor plate_color(255.*power,0,255.*(1.-power)); // power: 0.0 - blue, 1.0 - red


    draw3DBox   (DIMMS_WIDTH, PLATE_UNIT,  3.*CORE_UNIT, plate_color);
    glTranslatef(DIMM_UNIT*3., PLATE_UNIT, 0.5*CORE_UNIT);
    for(int j=0;j<8;++j)
    {
        draw3DBox(DIMM_UNIT, CORE_UNIT, CORE_UNIT*2., Qt::darkBlue);

        glTranslatef(3.*DIMM_UNIT,0, 0.);
    }

    glPopMatrix();
}

void Widget::drawQPILink(float x, float y, float z, float fill1, float /* fill2*/ ,const QColor & c)
{
    glPushMatrix();
    drawPipe(x,y,z,fill1,c);
//    glTranslatef(0,0,1.*z);
//    drawPipe(x,y,z*0.5,fill2,Qt::green);
    glPopMatrix();
}

void Widget::drawDIMMChannels(float ox, float oy, float oz, float x, float y, float z, float fill,const QColor & c)
{
    // mem channels
    glPushMatrix();
    glTranslatef(ox,oy,oz);
    for(int i=0;i<4;++i)
    {
        drawPipe(x,y,z*0.7,fill,c);
        glTranslatef(0,0,z*1.4);
    }
    glPopMatrix();
}

void Widget::drawAll()
{
    glTranslatef(-(DIMMS_WIDTH*2.+CPU_WIDTH*2.+4.)/2.,0,0);

    drawDIMMMs(DRAMPower[0]);

    drawDIMMChannels(DIMMS_WIDTH,0,0,1.0,PLATE_UNIT,PLATE_UNIT,iMCUtil[0],Qt::darkMagenta);

    glTranslatef(DIMMS_WIDTH + 1.0,0,-0.9);
    drawCPU(coreUtil[0], NULL, PackagePower[0]);

    // QPI links
    glPushMatrix();
    glTranslatef(CPU_WIDTH,0,1.6*PLATE_UNIT);
    drawQPILink(1.5,PLATE_UNIT,PLATE_UNIT*1.5,QPIUtil[0],-1.,Qt::red);
    glTranslatef(0,0,5.*PLATE_UNIT);
    drawQPILink(1.5,PLATE_UNIT,PLATE_UNIT*1.5,QPIUtil[1],-1.,Qt::red);
    glPopMatrix();

    glTranslatef(CPU_WIDTH + 1.5,0,0);
    drawCPU(coreUtil[1], NULL, PackagePower[1]);

    drawDIMMChannels(CPU_WIDTH,0,0.9,1.0,PLATE_UNIT,PLATE_UNIT,iMCUtil[1],Qt::darkMagenta);

    glTranslatef(CPU_WIDTH + 1.0,0,0.9);
    drawDIMMMs(DRAMPower[1]);

}

static void qNormalizeAngle(int &angle)
{
     while (angle < 0)
         angle += 360 * 16;
     while (angle > 360 * 16)
         angle -= 360 * 16;
}

void Widget::setXRotation(int angle)
{
    qNormalizeAngle(angle);
    if (angle != xRot)
        xRot = angle;
}

void Widget::setYRotation(int angle)
 {
     qNormalizeAngle(angle);
     if (angle != yRot)
         yRot = angle;
 }

void Widget::setZRotation(int angle)
 {
     qNormalizeAngle(angle);
     if (angle != zRot)
         zRot = angle;
 }

void Widget::mousePressEvent(QMouseEvent *event)
{
     lastPos = event->pos();
}

void Widget::mouseMoveEvent(QMouseEvent *event)
{
     int dx = event->x() - lastPos.x();
     int dy = event->y() - lastPos.y();

     if (event->buttons() & Qt::LeftButton)
     {
         setXRotation(xRot + 1 * dy);
         setYRotation(yRot + 1 * dx);
         update();
     } else if (event->buttons() & Qt::RightButton)
     {
         setXRotation(xRot + 1 * dy);
         setZRotation(zRot + 1 * dx);
         update();
     }
     lastPos = event->pos();
}
