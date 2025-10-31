import express, { Express,Request,Response } from "express";
import opentelemetry, { SpanStatusCode } from "@opentelemetry/api";
import axios from 'axios';
import {Agent} from 'https';

const PORT: number = parseInt(process.env?.PORT || "5000");
const app: Express = express();

const baseUrl: string = `https://swapi.dev/api`;

const tracer = opentelemetry.trace.getTracer('app-tracer');

const fetch = async(url: string):Promise<any> => {
  const response = await await axios.get(`${baseUrl}${url}`,{
    httpsAgent: new Agent({
      rejectUnauthorized: false
      })
   });
   const { data } = response;
   return data;
}

app.get("/", (req: Request, res: Response): void => {
    tracer.startActiveSpan('app.main.ping',(span: any) => {
      span.addEvent('Getting server info.');
      res.send('Pong.');
      span.end();
    });
});

app.get("/people/:id", (req: Request,res: Response): void => {
  const { id } = req.params;
  tracer.startActiveSpan('app.main.getPeople',async (span: any) => {
    try{
      span.addEvent('Getting Swapi Data');
      const apiData = await fetch(`/people/${id}`);
      res.send(apiData);
      span.end();
    }catch(err){
      span.addEvent('Getting Error Data');
      span.recordException(err as any);
      span.setStatus({
        code: SpanStatusCode.ERROR,
        message: 'Error'
      });
      span.end();
    }
  });
});


app.get("/starships/:id",(req: Request,res: Response): void => {
  const { id } = req.params;
  tracer.startActiveSpan('app.main.getStarships',async (span: any) => {
    try{
      span.addEvent('Getting Swapi Data');
      const apiData = await fetch(`/starships/${id}`);
      res.send(apiData);
      span.end();
    }catch(err){
      span.addEvent('Getting Error Data');
      span.recordException(err as any);
      span.setStatus({
        code: SpanStatusCode.ERROR,
        message: 'Error'
      });
      span.end();
    }
  });
})

app.listen(PORT, () => {
  console.log(`Test app listening on http://localhost:${PORT}`);
});